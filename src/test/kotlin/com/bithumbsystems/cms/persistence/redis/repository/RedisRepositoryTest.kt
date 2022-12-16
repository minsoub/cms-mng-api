package com.bithumbsystems.cms.persistence.redis.repository

import com.bithumbsystems.cms.api.model.enums.RedisKeys.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be greater than`
import org.amshove.kluent.`should not be`
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.redisson.api.RListReactive
import org.redisson.api.RScoredSortedSetReactive
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@ExtendWith(SpringExtension::class)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@SpringBootTest
class RedisRepositoryTest @Autowired constructor(
    private val redisRepository: RedisRepository
) {

    private val id = UUID.randomUUID().toString().replace("-", "")

    companion object {
        private lateinit var target: TestData
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Order(1)
    @Test
    fun addOrUpdateRMapCacheValue() = runTest {
        redisRepository.addOrUpdateRMapCacheValue(
            mapKey = MAP_TEST_KEY,
            valueKey = id,
            value = TestData(id = id, name = "test", createDate = LocalDateTime.now()),
            clazz = TestData::class.java
        )?.id `should be equal to` id
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Order(2)
    @Test
    fun getRMapCache() = runTest {
        redisRepository.getRMapCache(mapKey = MAP_TEST_KEY, clazz = TestData::class.java) `should not be` null
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Order(3)
    @Test
    fun getRMapCacheValue() = runTest {
        redisRepository.getRMapCacheValue(mapKey = MAP_TEST_KEY, valueKey = id, clazz = TestData::class.java)?.id `should be equal to` id
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Order(4)
    @Test
    fun deleteRMapCacheValue() = runTest {
        redisRepository.deleteRMapCacheValue(mapKey = MAP_TEST_KEY, valueKey = id, clazz = TestData::class.java)

        redisRepository.getRMapCacheValue(mapKey = MAP_TEST_KEY, valueKey = id, clazz = TestData::class.java) `should be` null
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Order(5)
    @Test
    fun addRScoredSortedSetValue() = runTest {
        var test = TestData(id = UUID.randomUUID().toString().replace("-", ""), name = "test1", createDate = LocalDateTime.now())
        val first: Boolean? = redisRepository.addRScoredSortedSetValue(
            setKey = SORTED_SET_TEST_KEY,
            score = test.getScore(),
            value = test,
            clazz = TestData::class.java
        )

        test = TestData(id = id, name = "test12", createDate = LocalDateTime.now().plusMinutes(1))
        val second: Boolean? = redisRepository.addRScoredSortedSetValue(
            setKey = SORTED_SET_TEST_KEY,
            score = test.getScore(),
            value = test,
            clazz = TestData::class.java
        )

        test = TestData(
            id = UUID.randomUUID().toString().replace("-", ""),
            name = "test2",
            createDate = LocalDateTime.now().plusMinutes(1)
        )
        val third: Boolean? = redisRepository.addRScoredSortedSetValue(
            setKey = SORTED_SET_TEST_KEY,
            score = test.getScore(),
            value = test,
            clazz = TestData::class.java
        )

        test = TestData(
            id = UUID.randomUUID().toString().replace("-", ""),
            name = "test3",
            createDate = LocalDateTime.now().plusMinutes(2)
        )
        val fourth: Boolean? = redisRepository.addRScoredSortedSetValue(
            setKey = SORTED_SET_TEST_KEY,
            score = test.getScore(),
            value = test,
            clazz = TestData::class.java
        )

        first `should be` true
        second `should be` true
        third `should be` true
        fourth `should be` true
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Order(6)
    @Test
    fun getRScoredSortedSet() = runTest {
        val result: RScoredSortedSetReactive<TestData> =
            redisRepository.getRScoredSortedSet(setKey = SORTED_SET_TEST_KEY, clazz = TestData::class.java)
        result.iterator().collectList().awaitSingleOrNull()?.forEach {
            println(it)
        }

        result `should not be` null
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Order(7)
    @Test
    fun deleteRScoredSortedSet() = runTest {
        val target: TestData? =
            redisRepository.getRScoredSortedSet(setKey = SORTED_SET_TEST_KEY, clazz = TestData::class.java).first().awaitSingleOrNull()
        target?.let {
            redisRepository.deleteRScoredSortedSet(setKey = SORTED_SET_TEST_KEY, value = target, clazz = TestData::class.java)
        }
        redisRepository.getRScoredSortedSet(setKey = SORTED_SET_TEST_KEY, clazz = TestData::class.java)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Order(8)
    @Test
    fun deleteAllRScoredSortedSet() = runTest {
        redisRepository.deleteAllRScoredSortedSet(setKey = SORTED_SET_TEST_KEY, clazz = TestData::class.java)
        val result: RScoredSortedSetReactive<TestData> =
            redisRepository.getRScoredSortedSet(setKey = SORTED_SET_TEST_KEY, clazz = TestData::class.java)

        result.size().awaitSingleOrNull()?.equals(0) `should be` true
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Order(9)
    @Test
    fun addRListValue() = runTest {
        val first: Boolean? = redisRepository.addRListValue(
            listKey = LIST_TEST_KEY,
            value = TestData(id = UUID.randomUUID().toString().replace("-", ""), name = "test1", createDate = LocalDateTime.now()),
            clazz = TestData::class.java
        )
        target = TestData(id, "test2", LocalDateTime.now())
        val second: Boolean? = redisRepository.addRListValue(
            listKey = LIST_TEST_KEY,
            value = target,
            clazz = TestData::class.java
        )

        val third: Boolean? = redisRepository.addRListValue(
            listKey = LIST_TEST_KEY,
            value = TestData(id = UUID.randomUUID().toString().replace("-", ""), name = "test3", createDate = LocalDateTime.now()),
            clazz = TestData::class.java
        )

        first `should be` true
        second `should be` true
        third `should be` true
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Order(10)
    @Test
    fun addAllRListValue() = runTest {
        redisRepository.addAllRListValue(
            listKey = LIST_TEST_KEY,
            value = listOf(
                TestData(id = UUID.randomUUID().toString().replace("-", ""), name = "test11", createDate = LocalDateTime.now()),
                TestData(id = UUID.randomUUID().toString().replace("-", ""), name = "test22", createDate = LocalDateTime.now()),
                TestData(id = UUID.randomUUID().toString().replace("-", ""), name = "test33", createDate = LocalDateTime.now())
            ),
            clazz = TestData::class.java
        ) `should be` true
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Order(11)
    @Test
    fun getRList() = runTest {
        val result: RListReactive<TestData> = redisRepository.getRList(listKey = LIST_TEST_KEY, clazz = TestData::class.java)
        val list: MutableList<TestData>? =
            result.iterator().collectSortedList(compareBy<TestData?> { it?.createDate }.thenBy { it?.name }).awaitSingleOrNull()

        result.size().awaitSingleOrNull()?.`should be greater than`(0)
        list?.size?.`should be greater than`(0)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Order(12)
    @Test
    fun getRListValue() = runTest {
        val list: List<TestData>? = redisRepository.getRListValue(listKey = LIST_TEST_KEY, clazz = TestData::class.java)
        list?.forEach {
            println(it)
        }
        list?.size?.`should be greater than`(0)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Order(13)
    @Test
    fun getRListValueById() = runTest {
        redisRepository.getRListValueById(listKey = LIST_TEST_KEY, id, clazz = TestData::class.java)?.id.equals(id) `should be` true
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Order(14)
    @Test
    fun updateRListValueById() = runTest {
        target.name = "테스트2"
        redisRepository.updateRListValueById(listKey = LIST_TEST_KEY, id = id, updateValue = target, clazz = TestData::class.java) `should be` true

        val item: TestData? = redisRepository.getRListValue(listKey = LIST_TEST_KEY, clazz = TestData::class.java)?.find { it.id == id }
        item?.id.equals(id) `should be` true
        item?.name.equals(target.name) `should be` true
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Order(15)
    @Test
    fun deleteRListValue() = runTest {
        redisRepository.deleteRListValue(listKey = LIST_TEST_KEY, id = id, clazz = TestData::class.java)?.id.equals(id) `should be` true
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Order(16)
    @Test
    fun deleteRList() = runTest {
        redisRepository.deleteRList(listKey = LIST_TEST_KEY, clazz = TestData::class.java) `should be` true
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Order(17)
    @Test
    fun addRBucketAndGetRBucket() = runTest {
        val name = "test1"
        redisRepository.addOrUpdateRBucket(
            bucketKey = BUCKET_TEST_KEY,
            value = TestData(id = UUID.randomUUID().toString().replace("-", ""), name = name, createDate = LocalDateTime.now()),
            clazz = TestData::class.java
        )

        val result: TestData? = redisRepository.getRBucket(bucketKey = BUCKET_TEST_KEY, clazz = TestData::class.java).get().awaitSingleOrNull()

        result?.name.equals(name) `should be` true
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Order(18)
    @Test
    fun deleteRBucket() = runTest {
        val result: Boolean? = redisRepository.deleteRBucket(bucketKey = BUCKET_TEST_KEY, clazz = TestData::class.java)

        result `should be` true
    }

    data class TestData(
        val id: String,
        var name: String,
        val createDate: LocalDateTime
    )

    fun TestData.getScore() = createDate.toEpochSecond(ZoneOffset.UTC).toDouble()
}
