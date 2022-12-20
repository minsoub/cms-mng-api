package com.bithumbsystems.cms.api.model.response

import com.bithumbsystems.cms.api.model.enums.ErrorCode
import com.bithumbsystems.cms.api.model.enums.ResponseCode
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "응답")
data class Response<out T>(
    @Schema(description = "응답 코드")
    val result: ResponseCode = ResponseCode.SUCCESS,
    @Schema(description = "응답 본문")
    val data: T? = null
)

@Schema(description = "목록 응답 객체")
data class ListResponse<out T>(
    @Schema(description = "목록")
    val contents: List<T>,
    @Schema(description = "총 갯수")
    val totalCounts: Long
)

@Schema(description = "에러 응답")
data class ErrorData(
    @Schema(description = "에러 코드")
    val code: ErrorCode,
    @Schema(description = "에러 메시지")
    val message: String?
)
