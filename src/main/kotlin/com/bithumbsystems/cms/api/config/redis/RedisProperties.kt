package com.bithumbsystems.cms.api.config.redis

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "spring.redis")
class RedisProperties(
    val host: String,
    val port: Int
)
