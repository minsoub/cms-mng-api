package com.bithumbsystems.cms.persistence.redis.entity

import java.time.LocalDateTime

data class RedisThumbnail(
    val id: String,
    val title: String,
    val thumbnailUrl: String?,
    val createDate: LocalDateTime
)
