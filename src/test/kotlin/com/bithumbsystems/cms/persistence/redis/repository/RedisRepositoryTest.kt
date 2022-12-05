package com.bithumbsystems.cms.persistence.redis.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should not be`
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.redisson.api.RMapCacheReactive
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@ExtendWith(SpringExtension::class)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@SpringBootTest
class RedisRepositoryTest @Autowired constructor(
    private val redisRepository: RedisRepository,
) {

    private val id = UUID.randomUUID().toString().replace("-", "")

    @BeforeAll
    fun beforeAll() {
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Order(1)
    @Test
    fun addOrUpdateMap() = runTest {
        val result: TestData? = redisRepository.addOrUpdateMap("test", id, TestData(id, "test"), TestData::class.java)

        result?.id `should be equal to` id
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Order(2)
    @Test
    fun getMap() = runTest {
        val result: RMapCacheReactive<String, TestData> = redisRepository.getMap("test", TestData::class.java)

        result `should not be` null
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Order(3)
    @Test
    fun getValueFromMap() = runTest {
        val result: TestData? = redisRepository.getValueFromMap("test", id, TestData::class.java)

        result?.id `should be equal to` id
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Order(4)
    @Test
    fun deleteValueFromMap() = runTest {
        redisRepository.deleteValueFromMap("test", id, TestData::class.java)

        val result: TestData? = redisRepository.getValueFromMap("test", id, TestData::class.java)

        result `should be` null
    }

    data class TestData(
        val id: String,
        val name: String,
    )
}
