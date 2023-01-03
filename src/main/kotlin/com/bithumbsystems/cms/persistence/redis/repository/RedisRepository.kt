package com.bithumbsystems.cms.persistence.redis.repository

import com.bithumbsystems.cms.api.model.enums.RedisKeys
import com.bithumbsystems.cms.api.util.Logger
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.redisson.api.*
import org.redisson.codec.TypedJsonJacksonCodec
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class RedisRepository(
    private val redissonReactiveClient: RedissonReactiveClient,
    private val objectMapper: ObjectMapper
) {
    private val logger by Logger()

    companion object {
        private const val WAIT_TIME = 1000L
        private const val LEASE_TIME = 3000L
    }

    /**
     * 레디스에 맵 등록 및 수정
     * @param mapKey 맵 키
     * @param valueKey 값 키
     * @param value 등록 또는 수정할 데이터
     * @param clazz 등록 또는 수정할 데이터의 클래스
     */
    suspend fun <T> addOrUpdateRMapCacheValue(mapKey: RedisKeys, valueKey: String, value: T, clazz: Class<T>): Result<T?, Throwable> =
        withLock(lockName = mapKey) {
            getRMapCache(mapKey, clazz).put(valueKey, value).awaitSingleOrNull() ?: getRMapCacheValue(mapKey, valueKey, clazz)
        }

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
    suspend fun <T> deleteRMapCacheValue(mapKey: RedisKeys, valueKey: String, clazz: Class<T>): Result<T?, Throwable> = withLock(lockName = mapKey) {
        getRMapCache(mapKey, clazz).remove(valueKey).awaitSingleOrNull()
    }

    /**
     * 레디스 셋 등록
     * @param setKey 셋 키
     * @param score 등록할 데이터의 정렬 가중치
     * @param value 등록할 데이터
     * @param clazz 등록할 데이터의 클래스
     */
    suspend fun <T> addRScoredSortedSetValue(setKey: RedisKeys, score: Double, value: T, clazz: Class<T>): Result<Boolean?, Throwable> =
        withLock(lockName = setKey) {
            getRScoredSortedSet(setKey, clazz).add(score, value).awaitSingleOrNull()
        }

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
    suspend fun <T> deleteRScoredSortedSet(setKey: RedisKeys, value: T, clazz: Class<T>): Result<Boolean?, Throwable> = withLock(lockName = setKey) {
        getRScoredSortedSet(setKey, clazz).remove(value).awaitSingleOrNull()
    }

    /**
     * 레디스 셋에서 모든 값 삭제
     * @param setKey 삭제할 셋 키
     * @param clazz 대상 데이터의 클래스
     */
    suspend fun <T> deleteAllRScoredSortedSet(setKey: RedisKeys, clazz: Class<T>): Result<Boolean?, Throwable> = withLock(lockName = setKey) {
        val set: RScoredSortedSetReactive<T> = getRScoredSortedSet(setKey, clazz)
        set.removeAll(set.readAll().awaitSingleOrNull()).awaitSingleOrNull()
    }

    /**
     * 레디스에 리스트 등록
     * @param listKey 리스트 키
     * @param value 등록할 데이터
     * @param clazz 등록할 데이터의 클래스
     */
    suspend fun <T> addRListValue(listKey: RedisKeys, value: T, clazz: Class<T>): Result<Boolean?, Throwable> = withLock(lockName = listKey) {
        getRList(listKey, clazz).add(value).awaitSingleOrNull()
    }

    /**
     * 레디스에 모든 리스트 등록
     * @param listKey 리스트 키
     * @param value 등록할 데이터
     * @param clazz 등록할 데이터의 클래스
     */
    suspend fun <T> addAllRListValue(listKey: RedisKeys, value: List<T>, clazz: Class<T>): Result<Boolean?, Throwable> =
        withLock(lockName = listKey) {
            getRList(listKey, clazz).addAll(value).awaitSingleOrNull()
        }

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
    suspend fun <T> updateRListValueById(listKey: RedisKeys, id: String, updateValue: T, clazz: Class<T>): Result<Void?, Throwable> =
        withLock(lockName = listKey) {
            getIndexAndRListById(listKey, clazz, id).run {
                this.second?.let { index: Int ->
                    this.first.remove(index).awaitSingleOrNull()
                    this.first.add(index, updateValue)
                }
            }?.awaitSingleOrNull()
        }

    /**
     * 레디스에서 리스트 전체 삭제
     * @param listKey 리스트 키
     * @param clazz 삭제할 데이터의 클래스
     */
    suspend fun <T> deleteRList(listKey: RedisKeys, clazz: Class<T>): Result<Boolean?, Throwable> = withLock(lockName = listKey) {
        getRList(listKey, clazz).delete().awaitSingleOrNull()
    }

    /**
     * 레디스에서 리스트 값 삭제
     * @param listKey 리스트 키
     * @param id 삭제할 대상의 아이디
     * @param clazz 삭제할 데이터의 클래스
     */
    suspend fun <T> deleteRListValue(listKey: RedisKeys, id: String, clazz: Class<T>): Result<T?, Throwable> = withLock(lockName = listKey) {
        getIndexAndRListById(listKey, clazz, id).run {
            this.second?.let { index: Int ->
                this.first.remove(index).awaitSingle()
            }
        }
    }

    /**
     * 레디스에 버킷 등록 및 수정
     * @param bucketKey 버킷 키
     * @param value 등록 및 수정할 데이터
     * @param typeReference 등록 및 수정할 데이터의 TypeReference, example = object : TypeReference<List<TestData>>() {}
     */
    suspend fun <T> addOrUpdateRBucket(bucketKey: RedisKeys, value: T, typeReference: TypeReference<T>): Result<Void?, Throwable> =
        withLock(lockName = bucketKey) {
            getRBucket(bucketKey, typeReference).set(value).awaitSingleOrNull()
        }

    /**
     * 레디스에서 버킷 조회
     * @param bucketKey 버킷 키
     * @param typeReference 조회할 데이터의 TypeReference, example = object : TypeReference<List<TestData>>() {}
     */
    suspend fun <T> getRBucket(bucketKey: RedisKeys, typeReference: TypeReference<T>): RBucketReactive<T> = redissonReactiveClient.getBucket(
        bucketKey.name,
        TypedJsonJacksonCodec(typeReference, objectMapper)
    )

    /**
     * 레디스에서 버킷 삭제
     * @param bucketKey 버킷 키
     * @param typeReference 삭제할 데이터의 TypeReference, example = object : TypeReference<List<TestData>>() {}
     */
    suspend fun <T> deleteRBucket(bucketKey: RedisKeys, typeReference: TypeReference<T>): Result<Boolean?, Throwable> =
        withLock(lockName = bucketKey) {
            getRBucket(bucketKey, typeReference).delete().awaitSingleOrNull()
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

    private suspend fun <T> withLock(lockName: RedisKeys, function: suspend () -> T): Result<T, Throwable> = runSuspendCatching {
        val functionName: String = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk {
            it.skip(2)
                .findFirst()
                .orElse(null)
        }.methodName
        logger.debug("{} method 에서 {} LOCK 취득 시도", functionName, lockName)
        val lock: RLockReactive = redissonReactiveClient.getLock(lockName.name.plus("_LOCK"))

        if (!lock.tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.MILLISECONDS).awaitSingle()) {
            logger.error("{} method 에서 {} LOCK 을 획득할 수 없습니다.", functionName, lockName)
        } else {
            logger.debug("{} method 에서 {} LOCK 획득", functionName, lockName)
        }
        function.invoke().also {
            if (lock.forceUnlock().awaitSingle()) {
                logger.debug("{} method 에서 {} LOCK 해제", functionName, lockName)
            } else {
                logger.error("{} method 에서 {} LOCK 해제 실패", functionName, lockName)
            }
        }
    }
}
