package com.bithumbsystems.cms.api.config.coroutine

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "coroutine.io.pool")
data class CoroutineIOPoolProperties(
    val coreSize: Int = 0,
    val maxSize: Int = 0,
    val threadNamePrefix: String
)
