package com.example.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VideoInfo(
    val title: String,
    val duration: Double?,
    val thumbnail: String?,
    val uploader: String?,
    @Json(name = "formats_count") val formatsCount: Int?,
    @Json(name = "series_name") val seriesName: String?,
    @Json(name = "episode_name") val episodeName: String?,
    @Json(name = "episode_number") val episodeNumber: Int?,
    @Json(name = "season_name") val seasonName: String?,
    @Json(name = "season_number") val seasonNumber: Int?,
    @Json(name = "upload_date") val uploadDate: String?,
    val description: String?
)

@JsonClass(generateAdapter = true)
data class Format(
    @Json(name = "format_id") val formatId: String,
    val ext: String?,
    val resolution: String?,
    val filesize: Long?,
    val vcodec: String?,
    val acodec: String?,
    val synthetic: Boolean? = false
)

@JsonClass(generateAdapter = true)
data class FormatsResponse(
    val formats: List<Format>
)

@JsonClass(generateAdapter = true)
data class DownloadRequest(
    val url: String,
    val quality: String,
    @Json(name = "expires_in") val expiresIn: Int = 3600,
    val cookies: String? = null
)

@JsonClass(generateAdapter = true)
data class DownloadResponse(
    val status: Boolean,
    @Json(name = "job_id") val jobId: String?,
    val error: String?
)

@JsonClass(generateAdapter = true)
data class DownloadResult(
    val title: String,
    val author: String?,
    val thumbnail: String?,
    val duration: Long?,
    val series: String?,
    @Json(name = "season_number") val seasonNumber: Int?,
    @Json(name = "episode_number") val episodeNumber: Int?,
    val format: String?,
    val quality: String?,
    val url: String,
    @Json(name = "watch_url") val watchUrl: String
)

@JsonClass(generateAdapter = true)
data class JobStatusResponse(
    val status: String,
    @Json(name = "created_at") val createdAt: Double?,
    val result: DownloadResult?,
    val error: String?
)

@JsonClass(generateAdapter = true)
data class Comment(
    val id: String,
    val name: String,
    val text: String,
    val ts: Long
)

@JsonClass(generateAdapter = true)
data class CommentsResponse(
    val comments: List<Comment>,
    val count: Int
)

@JsonClass(generateAdapter = true)
data class PostCommentRequest(
    @Json(name = "video_id") val videoId: String,
    val text: String,
    val name: String
)

@JsonClass(generateAdapter = true)
data class LikeResponse(
    val count: Int
)

@JsonClass(generateAdapter = true)
data class LikeRequest(
    @Json(name = "video_id") val videoId: String,
    val action: String
)
