package com.bithumbsystems.cms.api.model.request

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "공지사항 배너 등록/삭제 요청")
data class NoticeBannerRequest(

    @Schema(description = "배너 여부", allowableValues = ["true", "false"], defaultValue = "false")
    val isBanner: Boolean = false
)
