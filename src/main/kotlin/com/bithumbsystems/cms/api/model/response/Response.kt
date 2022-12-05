package com.bithumbsystems.cms.api.model.response

import com.bithumbsystems.cms.api.model.enums.ErrorCode
import com.bithumbsystems.cms.api.model.enums.ResponseCode

data class Response<out T>(
    val result: ResponseCode = ResponseCode.SUCCESS,
    val data: T? = null,
)

data class PageResponse<out T>(
    val contents: List<T>,
    val totalCounts: Long,
    val currentPage: Int,
    val pageSize: Int,
)

data class ErrorData(
    val code: ErrorCode,
    val message: String?,
)
