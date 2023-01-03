package com.bithumbsystems.cms.api.model.request

import com.bithumbsystems.cms.api.model.enums.ActionType

data class EventReasonRequest(
    val accountId: String,
    val actionType: ActionType = ActionType.DOWNLOAD,
    val reason: String,
    val email: String,
    val description: String,
    val siteId: String,
    val ip: String
)
