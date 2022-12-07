package com.bithumbsystems.cms.api.model.response

import com.bithumbsystems.cms.api.util.MaskingUtil.getEmailMask
import com.bithumbsystems.cms.persistence.mongo.entity.CmsNoticeCategory
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "공지사항 카테고리 목록")
data class NoticeCategoryResponse(
    @Schema(description = "아이디")
    val id: String,
    @Schema(description = "카테고리명")
    val name: String,
    @Schema(description = "상태")
    val isUse: Boolean = true,
    @Schema(description = "삭제 여부")
    val isDelete: Boolean = false,
    @Schema(description = "생성자 이메일")
    val createAccountEmail: String,
    @Schema(description = "생성일시, 예시: 2022-12-07 11:11")
    val createDate: LocalDateTime,
    @Schema(description = "수정자 이메일")
    val updateAccountEmail: String? = null,
    @Schema(description = "수정일시, 예시: 2022-12-07 11:11")
    val updateDate: LocalDateTime? = null
)

/**
 * CmsNoticeCategory Entity를 NoticeCategoryResponse로 변환한다.
 * @return 마스킹 처리된 응답
 */
fun CmsNoticeCategory.toMaskingResponse() = NoticeCategoryResponse(
    id = id,
    name = name,
    isUse = isUse,
    isDelete = isDelete,
    createAccountEmail = createAccountEmail.getEmailMask(),
    createDate = createDate,
    updateAccountEmail = updateAccountEmail?.getEmailMask(),
    updateDate = updateDate
)
