package com.bithumbsystems.cms.api.config.redis

import com.bithumbsystems.cms.api.util.PortCheckUtil.findAvailablePort
import com.bithumbsystems.cms.api.util.PortCheckUtil.isRunning
import org.redisson.Redisson
import org.redisson.api.RedissonReactiveClient
import org.redisson.config.Config
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import redis.embedded.RedisServer
import java.io.IOException
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Configuration
class RedisConfig(
    private val redisProperties: RedisProperties
) {

    private var redisServer: RedisServer? = null

    @PostConstruct
    @Throws(IOException::class)
    fun redisServer() {
        val redisPort = if (isRunning(redisProperties.port)) findAvailablePort() else redisProperties.port

        redisServer = RedisServer.builder()
            .port(redisPort)
            .setting("maxmemory 128M")
            .build()
        redisServer?.start()

        val config = Config()
        config.useSingleServer().address = "redis://${redisProperties.host}:$redisPort"
    }

    @PreDestroy
    fun stopRedis() {
        redisServer?.stop()
    }

    @Bean
    fun redissonReactiveClient(): RedissonReactiveClient =
        Redisson.create().reactive()
}
