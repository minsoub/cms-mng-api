package com.bithumbsystems.cms.api.model.request

import org.springframework.http.codec.multipart.FilePart

data class FileUploadRequest(
    val fileName: String,
    val thumbnail: FilePart? = null
)
