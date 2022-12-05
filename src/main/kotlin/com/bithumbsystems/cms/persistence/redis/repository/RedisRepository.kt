package com.bithumbsystems.cms.persistence.redis.repository

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.redisson.api.RMapCacheReactive
import org.redisson.api.RedissonReactiveClient
import org.redisson.codec.TypedJsonJacksonCodec
import org.springframework.stereotype.Service

@Service
class RedisRepository(
    private val redissonReactiveClient: RedissonReactiveClient,
    private val objectMapper: ObjectMapper,
) {
    /**
     * 레디스에 맵 등록 및 수정
     * @param mapKey 맵 키
     * @param valueKey 값 키
     * @param value 등록 또는 수정할 데이터
     * @param clazz 등록 또는 수정할 데이터의 클래스
     */
    suspend fun <T> addOrUpdateMap(mapKey: String, valueKey: String, value: T, clazz: Class<T>): T? =
        getMap(mapKey, clazz).put(valueKey, value).awaitSingleOrNull() ?: getValueFromMap(mapKey, valueKey, clazz)

    /**
     * 레디스에서 맵 가져오기
     * @param mapKey 맵 키
     * @param clazz 대상 데이터의 클래스
     */
    suspend fun <T> getMap(mapKey: String, clazz: Class<T>): RMapCacheReactive<String, T> =
        redissonReactiveClient.getMapCache(mapKey, TypedJsonJacksonCodec(String::class.java, clazz, objectMapper))

    suspend fun <T> getValueFromMap(mapKey: String, valueKey: String, clazz: Class<T>): T? =
        getMap(mapKey, clazz).get(valueKey).awaitSingleOrNull()

    /**
     * 레디스 맵에서 삭제
     * @param mapKey 맵 키
     * @param valueKey 값 키
     * @param clazz 대상 데이터의 클래스
     */
    suspend fun <T> deleteValueFromMap(mapKey: String, valueKey: String, clazz: Class<T>): T? =
        getMap(mapKey, clazz).remove(valueKey).awaitSingleOrNull()
}
