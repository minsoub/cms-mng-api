package com.bithumbsystems.cms.api.config.resolver

class Account(
    val accountId: String,
    val roles: Set<String>,
    val email: String,
    val userIp: String,
    val mySiteId: String
)
