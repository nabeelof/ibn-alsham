package com.ibnalsham.app.data.upload

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UploadResponseDto(
    val result: Boolean = false,
    val message: String? = null,
    val path: String? = null,
    @SerialName("upload_id") val uploadId: Int? = null,
)

@Serializable
data class UploadListResponseDto(
    val data: List<UploadItemDto> = emptyList(),
    val result: Boolean = false,
    val status: Int = 0,
)

@Serializable
data class UploadItemDto(
    val id: Int? = null,
    @SerialName("file_original_name") val fileOriginalName: String? = null,
    @SerialName("file_name") val fileName: String? = null,
    val url: String? = null,
)
