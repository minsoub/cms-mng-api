package com.bithumbsystems.cms.api.config.coroutine

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CoroutineConfig(
    val coroutineIOPoolProperties: CoroutineIOPoolProperties
) {

    @Bean
    fun ioDispatcher(): CoroutineDispatcher =
        CustomThreadPoolTaskExecutor().apply {
            initialize()
            setThreadNamePrefix("${coroutineIOPoolProperties.threadNamePrefix}-")
            corePoolSize = coroutineIOPoolProperties.coreSize
            maxPoolSize = coroutineIOPoolProperties.maxSize
        }.asCoroutineDispatcher()
}
