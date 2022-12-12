package com.bithumbsystems.cms.persistence.redis.repository

import com.bithumbsystems.cms.api.model.enums.RedisKeys
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.redisson.api.RListReactive
import org.redisson.api.RMapCacheReactive
import org.redisson.api.RScoredSortedSetReactive
import org.redisson.api.RedissonReactiveClient
import org.redisson.codec.TypedJsonJacksonCodec
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class RedisRepository(
    private val redissonReactiveClient: RedissonReactiveClient,
    private val objectMapper: ObjectMapper
) {
    /**
     * 레디스에 맵 등록 및 수정
     * @param mapKey 맵 키
     * @param valueKey 값 키
     * @param value 등록 또는 수정할 데이터
     * @param clazz 등록 또는 수정할 데이터의 클래스
     */
    suspend fun <T> addOrUpdateRMapCacheValue(mapKey: RedisKeys, valueKey: String, value: T, clazz: Class<T>): T? =
        getRMapCache(mapKey, clazz).put(valueKey, value).awaitSingleOrNull() ?: getRMapCacheValue(mapKey, valueKey, clazz)

    /**
     * 레디스에서 맵 가져오기
     * @param mapKey 맵 키
     * @param clazz 대상 데이터의 클래스
     */
    suspend fun <T> getRMapCache(mapKey: RedisKeys, clazz: Class<T>): RMapCacheReactive<String, T> =
        redissonReactiveClient.getMapCache(mapKey.name, TypedJsonJacksonCodec(String::class.java, clazz, objectMapper))

    suspend fun <T> getRMapCacheValue(mapKey: RedisKeys, valueKey: String, clazz: Class<T>): T? =
        getRMapCache(mapKey, clazz).get(valueKey).awaitSingleOrNull()

    /**
     * 레디스 맵에서 삭제
     * @param mapKey 맵 키
     * @param valueKey 값 키
     * @param clazz 대상 데이터의 클래스
     */
    suspend fun <T> deleteRMapCacheValue(mapKey: RedisKeys, valueKey: String, clazz: Class<T>): T? =
        getRMapCache(mapKey, clazz).remove(valueKey).awaitSingleOrNull()

    /**
     * 레디스 셋 등록
     * @param setKey 셋 키
     * @param score 등록할 데이터의 정렬 가중치
     * @param value 등록할 데이터
     * @param clazz 등록할 데이터의 클래스
     */
    suspend fun <T> addRScoredSortedSetValue(setKey: RedisKeys, score: Double, value: T, clazz: Class<T>): Boolean? =
        getRScoredSortedSet(setKey, clazz).add(score, value).awaitSingleOrNull()

    /**
     * 레디스에서 셋 조회
     * @param setKey 셋 키
     * @param clazz 조회할 데이터의 클래스
     */
    suspend fun <T> getRScoredSortedSet(setKey: RedisKeys, clazz: Class<T>): RScoredSortedSetReactive<T> =
        redissonReactiveClient.getScoredSortedSet(setKey.name, TypedJsonJacksonCodec(clazz, objectMapper))

    /**
     * 레디스에서 객체 삭제
     * @param setKey 셋 키
     * @param value 삭제할 객체의 값
     * @param clazz 삭제할 데이터의 클래스
     */
    suspend fun <T> deleteRScoredSortedSet(setKey: RedisKeys, value: T, clazz: Class<T>): Boolean? =
        getRScoredSortedSet(setKey, clazz).remove(value).awaitSingleOrNull()

    /**
     * 레디스 셋에서 모든 값 삭제
     * @param setKey 삭제할 셋 키
     * @param clazz 대상 데이터의 클래스
     */
    suspend fun <T> deleteAllRScoredSortedSet(setKey: RedisKeys, clazz: Class<T>) {
        val set: RScoredSortedSetReactive<T> = getRScoredSortedSet(setKey, clazz)
        set.removeAll(set.readAll().awaitSingleOrNull()).awaitSingleOrNull()
    }

    /**
     * 레디스에 리스트 등록
     * @param listKey 리스트 키
     * @param value 등록할 데이터
     * @param clazz 등록할 데이터의 클래스
     */
    suspend fun <T> addRListValue(listKey: RedisKeys, value: T, clazz: Class<T>): Boolean? =
        getRList(listKey, clazz).add(value).awaitSingleOrNull()

    /**
     * 레디스에 모든 리스트 등록
     * @param listKey 리스트 키
     * @param value 등록할 데이터
     * @param clazz 등록할 데이터의 클래스
     */
    suspend fun <T> addAllRListValue(listKey: RedisKeys, value: List<T>, clazz: Class<T>): Boolean? =
        getRList(listKey, clazz).addAll(value).awaitSingleOrNull()

    /**
     * 레디스에서 리스트 조회(RListReactive)
     * @param listKey 리스트 키
     * @param clazz 조회할 데이터의 클래스
     */
    suspend fun <T> getRList(listKey: RedisKeys, clazz: Class<T>): RListReactive<T> =
        redissonReactiveClient.getList(listKey.name, TypedJsonJacksonCodec(clazz, objectMapper))

    /**
     * 레디스에서 리스트 조회
     * @param listKey 리스트 키
     * @param clazz 조회할 데이터의 클래스
     */
    suspend fun <T> getRListValue(listKey: RedisKeys, clazz: Class<T>): MutableList<T>? =
        getRList(listKey, clazz).iterator().collectList().awaitSingleOrNull()

    /**
     * 레디스에서 리스트 조회 후 id로 조회
     * @param listKey 리스트 키
     * @param id 꺼낼 대상의 아이디
     * @param clazz 조회할 데이터의 클래스
     */
    suspend fun <T> getRListValueById(listKey: RedisKeys, id: String, clazz: Class<T>): T? = getRListValue(listKey, clazz)?.find {
        it.toString().contains("id=$id")
    }

    /**
     * 레디스에서 id로 수정
     * @param listKey 리스트 키
     * @param id 수정할 대상의 아이디
     * @param updateValue 수정할 값
     * @param clazz 수정할 데이터의 클래스
     */
    suspend fun <T> updateRListValueById(listKey: RedisKeys, id: String, updateValue: T, clazz: Class<T>): Boolean? =
        getIndexAndRListById(listKey, clazz, id).run {
            this.second?.let { index: Int ->
                this.first.remove(index).awaitSingleOrNull()
                this.first.add(index, updateValue) as Mono<Boolean>
            }
        }?.awaitSingleOrNull()

    /**
     * 레디스에서 리스트 전체 삭제
     * @param listKey 리스트 키
     * @param clazz 삭제할 데이터의 클래스
     */
    suspend fun <T> deleteRList(listKey: RedisKeys, clazz: Class<T>): Boolean? =
        getRList(listKey, clazz).delete().awaitSingleOrNull()

    /**
     * 레디스에서 리스트 값 삭제
     * @param listKey 리스트 키
     * @param id 삭제할 대상의 아이디
     * @param clazz 삭제할 데이터의 클래스
     */
    suspend fun <T> deleteRListValue(listKey: RedisKeys, id: String, clazz: Class<T>): T? =
        getIndexAndRListById(listKey, clazz, id).run {
            this.second?.let { index: Int ->
                this.first.remove(index).awaitSingle()
            }
        }

    private suspend fun <T> getIndexAndRListById(
        listKey: RedisKeys,
        clazz: Class<T>,
        id: String
    ): Pair<RListReactive<T>, Int?> {
        val rList: RListReactive<T> = getRList(listKey, clazz)
        return Pair(
            rList,
            getRListValueById(listKey, id, clazz)?.let {
                rList.indexOf(it)?.awaitSingleOrNull()
            }
        )
    }
}
