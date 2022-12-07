package com.bithumbsystems.cms.api.model.request

import com.bithumbsystems.cms.persistence.mongo.entity.CmsNoticeCategory
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "공지사항 카테고리 요청")
data class NoticeCategoryRequest(
    @Schema(description = "카테고리명")
    val name: String,
    @Schema(description = "상태", allowableValues = ["true", "false"])
    val isUse: Boolean,
    @Schema(description = "삭제 여부")
    var isDelete: Boolean = false
) {
    @Schema(description = "생성자 아이디")
    var createAccountId: String = ""

    @Schema(description = "생성자 이메일")
    var createAccountEmail: String = ""
}

fun NoticeCategoryRequest.toEntity(): CmsNoticeCategory = CmsNoticeCategory(
    name = name,
    isUse = isUse,
    isDelete = isDelete,
    createAccountId = createAccountId,
    createAccountEmail = createAccountEmail
)
