package com.example

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.*
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

sealed interface FetchState {
    object Idle : FetchState
    object Loading : FetchState
    data class Success(val info: VideoInfo, val formats: List<Format>) : FetchState
    data class Error(val message: String) : FetchState
}

sealed interface DownloadJobState {
    object Idle : DownloadJobState
    data class Processing(val progress: Int, val label: String) : DownloadJobState
    data class Success(val result: DownloadResult) : DownloadJobState
    data class Error(val message: String) : DownloadJobState
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = HistoryRepository(database.historyDao())
    val settingsManager = SettingsManager(application)

    // Observable states
    private val _baseUrlState = MutableStateFlow(settingsManager.baseUrl)
    val baseUrlState: StateFlow<String> = _baseUrlState.asStateFlow()

    private val _usernameState = MutableStateFlow(settingsManager.username)
    val usernameState: StateFlow<String> = _usernameState.asStateFlow()

    private val _cookiesState = MutableStateFlow(settingsManager.cookies)
    val cookiesState: StateFlow<String> = _cookiesState.asStateFlow()

    private val _fetchState = MutableStateFlow<FetchState>(FetchState.Idle)
    val fetchState: StateFlow<FetchState> = _fetchState.asStateFlow()

    private val _downloadJobState = MutableStateFlow<DownloadJobState>(DownloadJobState.Idle)
    val downloadJobState: StateFlow<DownloadJobState> = _downloadJobState.asStateFlow()

    // History Flow
    val historyList: StateFlow<List<HistoryEntity>> = repository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Player screen states
    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    private val _likesCount = MutableStateFlow(0)
    val likesCount: StateFlow<Int> = _likesCount.asStateFlow()

    private val _isLiked = MutableStateFlow(false)
    val isLiked: StateFlow<Boolean> = _isLiked.asStateFlow()

    // Formats categorizer lists
    val combinedFormats = MutableStateFlow<List<Format>>(emptyList())
    val videoFormats = MutableStateFlow<List<Format>>(emptyList())
    val audioFormats = MutableStateFlow<List<Format>>(emptyList())

    private var activePollJob: Job? = null

    fun updateBaseUrl(newUrl: String) {
        settingsManager.baseUrl = newUrl
        _baseUrlState.value = newUrl
    }

    fun updateUsername(newName: String) {
        settingsManager.username = newName
        _usernameState.value = newName
    }

    fun updateCookies(newCookies: String) {
        settingsManager.cookies = newCookies
        _cookiesState.value = newCookies
    }

    // Fetches info and formats
    fun fetchVideoMetadata(videoUrl: String) {
        if (videoUrl.isBlank()) {
            _fetchState.value = FetchState.Error("URL field cannot be empty")
            return
        }

        viewModelScope.launch {
            _fetchState.value = FetchState.Loading
            _downloadJobState.value = DownloadJobState.Idle
            try {
                val api = ApiClient.getService(settingsManager.baseUrl)
                
                // Fetch info and formats in parallel or sequence
                val cookiesStr = settingsManager.cookies.takeIf { it.isNotBlank() }
                val info = api.getVideoInfo(videoUrl, cookiesStr)
                val formatsRes = api.getFormats(videoUrl, cookiesStr)
                
                val allFormats = formatsRes.formats
                val hasVideo = { f: Format -> f.vcodec != null && f.vcodec != "none" }
                val hasAudio = { f: Format -> f.acodec != null && f.acodec != "none" }

                val realCombined = allFormats.filter { hasVideo(it) && hasAudio(it) }
                val vOnly = allFormats.filter { hasVideo(it) && !hasAudio(it) }
                val aOnly = allFormats.filter { !hasVideo(it) && hasAudio(it) }

                videoFormats.value = vOnly
                audioFormats.value = aOnly

                // Mirror synthesis pairing logic of Flask/JS frontend
                if (realCombined.isNotEmpty()) {
                    combinedFormats.value = realCombined
                } else if (vOnly.isNotEmpty() && aOnly.isNotEmpty()) {
                    val bestAudio = aOnly.maxByOrNull { it.filesize ?: 0L }
                    if (bestAudio != null) {
                        combinedFormats.value = vOnly.map { v ->
                            Format(
                                formatId = "${v.formatId}+${bestAudio.formatId}",
                                resolution = v.resolution,
                                ext = v.ext,
                                vcodec = v.vcodec,
                                acodec = bestAudio.acodec,
                                filesize = (v.filesize ?: 0L) + (bestAudio.filesize ?: 0L),
                                synthetic = true
                            )
                        }
                    } else {
                        combinedFormats.value = emptyList()
                    }
                } else {
                    combinedFormats.value = emptyList()
                }

                _fetchState.value = FetchState.Success(info, allFormats)
            } catch (e: Exception) {
                _fetchState.value = FetchState.Error(e.message ?: "Failed to load metadata. Please verify server URL.")
            }
        }
    }

    // Starts download and polls status
    fun startAndPollDownload(videoUrl: String, selectedQualityId: String) {
        activePollJob?.cancel()
        _downloadJobState.value = DownloadJobState.Processing(0, "Initiating download job...")

        activePollJob = viewModelScope.launch {
            try {
                val api = ApiClient.getService(settingsManager.baseUrl)
                val response = api.startDownload(
                    DownloadRequest(
                        url = videoUrl,
                        quality = selectedQualityId,
                        cookies = settingsManager.cookies.takeIf { it.isNotBlank() }
                    )
                )
                
                if (!response.status || response.jobId == null) {
                    _downloadJobState.value = DownloadJobState.Error(response.error ?: "Failed to start server download job")
                    return@launch
                }

                val jobId = response.jobId
                var elapsedSec = 0
                var isCompleted = false

                _downloadJobState.value = DownloadJobState.Processing(5, "Downloading stream from source...")

                // Poll status every 3 seconds
                while (!isCompleted) {
                    delay(3000)
                    elapsedSec += 3
                    
                    try {
                        val statusRes = api.getJobStatus(jobId)
                        when (statusRes.status) {
                            "pending" -> {
                                val progress = minOf(95, 5 + elapsedSec)
                                _downloadJobState.value = DownloadJobState.Processing(
                                    progress = progress,
                                    label = "Downloading & Processing video fragments... ($elapsedSec s elapsed)"
                                )
                            }
                            "done" -> {
                                isCompleted = true
                                val result = statusRes.result
                                if (result != null) {
                                    _downloadJobState.value = DownloadJobState.Success(result)
                                    // Save to history list reactively
                                    repository.insert(
                                        HistoryEntity(
                                            url = videoUrl,
                                            title = result.title,
                                            thumbnail = result.thumbnail ?: "",
                                            duration = result.duration ?: 0L,
                                            seriesName = result.series,
                                            episodeName = result.episodeNumber?.let { "Episode $it" },
                                            seasonNumber = result.seasonNumber,
                                            episodeNumber = result.episodeNumber,
                                            uploader = result.author,
                                            quality = result.quality,
                                            format = result.format,
                                            downloadUrl = result.url,
                                            watchUrl = result.watchUrl
                                        )
                                    )
                                } else {
                                    _downloadJobState.value = DownloadJobState.Error("Server job finished but no download links returned.")
                                }
                            }
                            "error" -> {
                                isCompleted = true
                                _downloadJobState.value = DownloadJobState.Error(statusRes.error ?: "Download job failed on server")
                            }
                        }
                    } catch (e: Exception) {
                        // Continue on transient network issues during polling
                        _downloadJobState.value = DownloadJobState.Processing(
                            progress = minOf(95, 5 + elapsedSec),
                            label = "Reconnecting with server..."
                        )
                    }
                }
            } catch (e: Exception) {
                _downloadJobState.value = DownloadJobState.Error(e.message ?: "Failed to start download process")
            }
        }
    }

    // Fetches comments for specific video
    fun loadComments(videoId: String) {
        viewModelScope.launch {
            try {
                val api = ApiClient.getService(settingsManager.baseUrl)
                val response = api.getComments(videoId)
                _comments.value = response.comments
            } catch (e: Exception) {
                // Ignore transient errors
            }
        }
    }

    // Posts a comment
    fun postComment(videoId: String, text: String) {
        if (text.isBlank()) return
        val currentName = settingsManager.username

        viewModelScope.launch {
            try {
                val api = ApiClient.getService(settingsManager.baseUrl)
                api.postComment(PostCommentRequest(videoId, text, currentName))
                loadComments(videoId) // Reload list
            } catch (e: Exception) {
                // Ignore transient errors
            }
        }
    }

    // Fetches likes count for video
    fun loadLikes(videoId: String) {
        viewModelScope.launch {
            try {
                val api = ApiClient.getService(settingsManager.baseUrl)
                val response = api.getLikes(videoId)
                _likesCount.value = response.count
            } catch (e: Exception) {
                // Ignore transient errors
            }
        }
    }

    // Posts a like
    fun toggleLike(videoId: String) {
        val wasLiked = _isLiked.value
        val action = if (wasLiked) "unlike" else "like"
        _isLiked.value = !wasLiked

        viewModelScope.launch {
            try {
                val api = ApiClient.getService(settingsManager.baseUrl)
                val response = api.postLike(LikeRequest(videoId, action))
                _likesCount.value = response.count
            } catch (e: Exception) {
                // Revert state on failure
                _isLiked.value = wasLiked
            }
        }
    }

    // Downloads file to device storage via system DownloadManager
    fun downloadToDevice(context: Context, downloadUrl: String, title: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
                setTitle(title)
                setDescription("Downloading video via OURIN Downloader")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                val fileExt = if (downloadUrl.contains(".mp3")) "mp3" else "mp4"
                setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    "${title.replace("[^a-zA-Z0-9.-]".toRegex(), "_")}.$fileExt"
                )
            }
            val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            manager.enqueue(request)
            Toast.makeText(context, "Download started. Check notifications.", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to start download: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun deleteHistoryItem(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clear()
        }
    }

    override fun onCleared() {
        super.onCleared()
        activePollJob?.cancel()
    }
}
