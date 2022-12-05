package com.bithumbsystems.cms.api.util

object NullUtil {

    /**
     * let(if not null)의 다중 사용을 위한 함수
     */
    inline fun <T : Any, R : Any> letIfAllNotNull(vararg arguments: T?, block: (List<T>) -> R): R? {
        return if (arguments.all { it != null }) {
            block(arguments.filterNotNull())
        } else null
    }
}
