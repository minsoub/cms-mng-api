package com.bithumbsystems.cms.api.model.response

import com.bithumbsystems.cms.persistence.mongo.entity.CmsNoticeCategory
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "공시사항 카테고리 상세")
data class NoticeCategoryDetailResponse(
    @Schema(description = "아이디", example = "b40f760a9ce84702905347b1e0d98aeb")
    val id: String,
    @Schema(description = "카테고리명", example = "카테고리명")
    val name: String,
    @Schema(description = "상태(공개 여부)", example = "false")
    val isUse: Boolean = true,
    @Schema(description = "삭제 여부", example = "false")
    val isDelete: Boolean = false,
    @Schema(description = "생성자 아이디", example = "bda0f8f03f8e11edb8780242ac120002")
    val createAccountId: String,
    @Schema(description = "생성자 이메일", example = "abc@example.com")
    val createAccountEmail: String,
    @Schema(description = "생성일시, 예시: 2022-12-07 11:11:11", example = "2022-12-07 11:11:11")
    val createDate: LocalDateTime,
    @Schema(description = "수정자 아이디", example = "bda0f8f03f8e11edb8780242ac120002")
    val updateAccountId: String? = null,
    @Schema(description = "수정자 이메일", example = "abc@example.com")
    val updateAccountEmail: String? = null,
    @Schema(description = "수정일시, 예시: 2022-12-07 11:11:11", example = "2022-12-07 11:11:11")
    val updateDate: LocalDateTime? = null
)

fun NoticeCategoryDetailResponse.toEntity(): CmsNoticeCategory {
    val entity = CmsNoticeCategory(
        id = id,
        name = name,
        isUse = isUse,
        isDelete = isDelete,
        createAccountId = createAccountId,
        createAccountEmail = createAccountEmail,
        createDate = createDate
    )
    entity.updateAccountId = updateAccountId
    entity.updateAccountEmail = updateAccountEmail
    entity.updateDate = updateDate
    return entity
}

/**
 * CmsNoticeCategory Entity를 NoticeCategoryDetailResponse 변환한다.
 * @return 마스킹 처리되지 않은 응답
 */
fun CmsNoticeCategory.toResponse(): NoticeCategoryDetailResponse = NoticeCategoryDetailResponse(
    id = id,
    name = name,
    isUse = isUse,
    isDelete = isDelete,
    createAccountId = createAccountId,
    createAccountEmail = createAccountEmail,
    createDate = createDate,
    updateAccountId = updateAccountId,
    updateAccountEmail = updateAccountEmail,
    updateDate = updateDate
)
