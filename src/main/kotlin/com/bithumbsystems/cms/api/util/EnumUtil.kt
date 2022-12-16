package com.bithumbsystems.cms.api.util

object EnumUtil {
    inline infix fun <reified E : Enum<E>, V> ((E) -> V).findBy(value: V): E? {
        return enumValues<E>().firstOrNull { this(it).toString().lowercase() == value.toString().lowercase() }
    }
}
