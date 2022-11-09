package com.bithumbsystems.cms.api.config.coroutine

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import kotlin.math.max

class CustomThreadPoolTaskExecutor : ThreadPoolTaskExecutor() {

    companion object {
        /**
         * Default core pool size. Fallback to
         * available number of processors (but with a minimum value of 5)
         */
        val DEFAULT_CORE_POOL_SIZE = max(Runtime.getRuntime().availableProcessors(), 2) * 2 + 1

        /**
         * Default max pool size. Fallback to
         * available number of processors (but with a minimum value of 33)
         */
        val DEFAULT_MAX_POOL_SIZE = max(Runtime.getRuntime().availableProcessors(), 8) * 4 + 1
    }

    override fun setCorePoolSize(corePoolSize: Int) {
        val poolSize = if (corePoolSize <= 0) DEFAULT_CORE_POOL_SIZE else corePoolSize
        super.setCorePoolSize(poolSize)
    }

    override fun setMaxPoolSize(maxPoolSize: Int) {
        val poolSize = if (maxPoolSize <= 0) DEFAULT_MAX_POOL_SIZE else maxPoolSize
        super.setMaxPoolSize(poolSize)
    }
}
