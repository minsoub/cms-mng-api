package com.bithumbsystems.cms.api.model.response

import com.bithumbsystems.cms.api.util.MaskingUtil.getEmailMask
import com.bithumbsystems.cms.persistence.mongo.entity.CmsNoticeCategory
import java.time.LocalDateTime

data class NoticeCategoryResponse(
    val id: String,
    val name: String,
    val isUse: Boolean = true,
    val createAccountId: String,
    val createAccountEmail: String,
    val createDate: LocalDateTime,
    val updateAccountId: String? = null,
    val updateAccountEmail: String? = null,
    val updateDate: LocalDateTime? = null,
)

fun NoticeCategoryResponse.toEntity(): CmsNoticeCategory = CmsNoticeCategory(
    id = id,
    name = name,
    isUse = isUse,
    createAccountId = createAccountId,
    createAccountEmail = createAccountEmail,
    createDate = createDate,
    updateAccountId = updateAccountId,
    updateAccountEmail = updateAccountEmail,
    updateDate = updateDate
)

/**
 * CmsNoticeCategory Entity를 NoticeCategoryResponse로 변환한다.
 * @return 마스킹 처리되지 않은 응답
 */
fun CmsNoticeCategory.toResponse() = NoticeCategoryResponse(
    id = id,
    name = name,
    isUse = isUse,
    createAccountId = createAccountId,
    createAccountEmail = createAccountEmail,
    createDate = createDate,
    updateAccountId = updateAccountId,
    updateAccountEmail = updateAccountEmail,
    updateDate = updateDate
)

/**
 * CmsNoticeCategory Entity를 NoticeCategoryResponse로 변환한다.
 * @return 마스킹 처리된 응답
 */
fun CmsNoticeCategory.toMaskingResponse() = NoticeCategoryResponse(
    id = id,
    name = name,
    isUse = isUse,
    createAccountId = createAccountId,
    createAccountEmail = createAccountEmail.getEmailMask(),
    createDate = createDate,
    updateAccountId = updateAccountId,
    updateAccountEmail = updateAccountEmail?.getEmailMask(),
    updateDate = updateDate
)
