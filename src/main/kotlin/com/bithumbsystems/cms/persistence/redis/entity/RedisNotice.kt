package com.bithumbsystems.cms.persistence.redis.entity

import java.time.LocalDateTime

data class RedisNotice(
    val id: String,
    val title: String,
    val categoryName: List<String>,
    val screenDate: LocalDateTime
)
