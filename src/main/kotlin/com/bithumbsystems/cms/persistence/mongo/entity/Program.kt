package com.bithumbsystems.cms.persistence.mongo.entity

import com.bithumbsystems.cms.persistence.mongo.enums.ActionMethod
import com.bithumbsystems.cms.persistence.mongo.enums.RoleType

data class Program(
    val name: String? = "",
    val type: RoleType,
    val kindName: String? = "",
    val actionMethod: ActionMethod,
    val actionUrl: String,
    val isUse: Boolean,
    val description: String? = "",
    val siteId: String
)
