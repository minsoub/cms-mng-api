package com.bithumbsystems.cms.api.exception

import software.amazon.awssdk.core.SdkResponse
import java.util.*

class UploadFailedException(response: SdkResponse) : RuntimeException() {
    private var statusCode = 0
    private var statusText: Optional<String>? = null

    init {
        response.sdkHttpResponse()?.let {
            statusCode = it.statusCode()
            statusText = it.statusText()
        }
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
