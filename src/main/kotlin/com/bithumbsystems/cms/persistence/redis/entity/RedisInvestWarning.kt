package com.bithumbsystems.cms.persistence.redis.entity

import java.time.LocalDateTime

data class RedisInvestWarning(
    val id: String,
    val title: String,
    val screenDate: LocalDateTime
)
