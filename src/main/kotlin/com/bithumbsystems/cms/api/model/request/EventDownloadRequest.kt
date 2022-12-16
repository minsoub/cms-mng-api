package com.bithumbsystems.cms.api.model.request

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "이벤트 다운로드 요청")
class EventDownloadRequest(
    @Schema(description = "다운로드 사유")
    val reason: String
)
