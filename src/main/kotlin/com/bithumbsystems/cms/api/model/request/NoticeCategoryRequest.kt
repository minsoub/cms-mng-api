package com.bithumbsystems.cms.api.model.request

import com.bithumbsystems.cms.persistence.mongo.entity.CmsNoticeCategory

data class NoticeCategoryRequest(
    val name: String,
    val isUse: Boolean
) {
    var createAccountId: String = ""
    var createAccountEmail: String = ""
}

fun NoticeCategoryRequest.toEntity(): CmsNoticeCategory = CmsNoticeCategory(
    name = name,
    isUse = isUse,
    createAccountId = createAccountId,
    createAccountEmail = createAccountEmail
)
