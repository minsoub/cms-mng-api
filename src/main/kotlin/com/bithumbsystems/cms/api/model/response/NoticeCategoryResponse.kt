package com.bithumbsystems.cms.api.model.response

import com.bithumbsystems.cms.api.util.MaskingUtil.getEmailMask
import com.bithumbsystems.cms.persistence.mongo.entity.CmsNoticeCategory
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "공지사항 카테고리 목록")
data class NoticeCategoryResponse(
    @Schema(description = "아이디", example = "b40f760a9ce84702905347b1e0d98aeb")
    val id: String,
    @Schema(description = "카테고리명", example = "카테고리명")
    val name: String,
    @Schema(description = "상태(공개 여부)", example = "false")
    val isUse: Boolean = true,
    @Schema(description = "생성자 이메일", example = "abc@example.com")
    val createAccountEmail: String,
    @Schema(description = "생성일시, 예시: 2022-12-07 11:11:11", example = "2022-12-07 11:11:11")
    val createDate: LocalDateTime,
    @Schema(description = "수정자 이메일", example = "abc@example.com")
    val updateAccountEmail: String? = null,
    @Schema(description = "수정일시, 예시: 2022-12-07 11:11:11", example = "2022-12-07 11:11:11")
    val updateDate: LocalDateTime? = null
)

@Schema(description = "공지사항 카테고리 목록")
data class CategoryResponse(
    @Schema(description = "아이디", example = "b40f760a9ce84702905347b1e0d98aeb")
    val id: String,
    @Schema(description = "카테고리명", example = "카테고리명")
    val name: String,
    @Schema(description = "생성일시, 예시: 2022-12-07 11:11:11", example = "2022-12-07 11:11:11")
    val createDate: LocalDateTime
)

/**
 * CmsNoticeCategory Entity를 NoticeCategoryResponse로 변환한다.
 * @return 마스킹 처리된 응답
 */
fun CmsNoticeCategory.toMaskingResponse(): NoticeCategoryResponse = NoticeCategoryResponse(
    id = id,
    name = name,
    isUse = isUse,
    createAccountEmail = createAccountEmail.getEmailMask(),
    createDate = createDate,
    updateAccountEmail = updateAccountEmail?.getEmailMask(),
    updateDate = updateDate
)

fun CmsNoticeCategory.toCategoryMaskingResponse(): CategoryResponse = CategoryResponse(
    id = id,
    name = name,
    createDate = createDate
)
