package com.bithumbsystems.cms.persistence.redis.entity

import java.time.LocalDateTime

data class RedisWithThumbnail(
    val id: String,
    val title: String,
    val thumbnailFileId: String?, // todo
    val screenDate: LocalDateTime
)
