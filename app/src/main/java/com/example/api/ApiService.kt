package com.example.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @GET("info")
    suspend fun getVideoInfo(
        @Query("url") url: String,
        @Header("X-Cookies") cookies: String? = null
    ): VideoInfo

    @GET("formats")
    suspend fun getFormats(
        @Query("url") url: String,
        @Header("X-Cookies") cookies: String? = null
    ): FormatsResponse

    @POST("download")
    suspend fun startDownload(
        @Body request: DownloadRequest
    ): DownloadResponse

    @GET("download/status")
    suspend fun getJobStatus(
        @Query("job_id") jobId: String
    ): JobStatusResponse

    @GET("comments")
    suspend fun getComments(
        @Query("video_id") videoId: String
    ): CommentsResponse

    @POST("comments")
    suspend fun postComment(
        @Body request: PostCommentRequest
    ): DownloadResponse // or status response with status boolean

    @GET("like")
    suspend fun getLikes(
        @Query("video_id") videoId: String
    ): LikeResponse

    @POST("like")
    suspend fun postLike(
        @Body request: LikeRequest
    ): LikeResponse
}
