package com.bithumbsystems.cms.api.util

import java.time.LocalDateTime

object ResponseUtil {
    fun checkIsUseUpdateDate(isUseUpdateDate: Boolean, screenDate: LocalDateTime?, createDate: LocalDateTime): LocalDateTime {
        return if (isUseUpdateDate) screenDate ?: createDate else createDate
    }
}
