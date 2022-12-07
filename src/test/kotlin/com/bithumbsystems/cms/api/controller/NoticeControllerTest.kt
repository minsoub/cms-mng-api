package com.bithumbsystems.cms.api.controller

import com.bithumbsystems.cms.api.model.enums.ResponseCode
import com.bithumbsystems.cms.api.model.request.NoticeCategoryRequest
import com.bithumbsystems.cms.api.model.response.Response
import org.amshove.kluent.`should be`
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import java.util.*

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class NoticeControllerTest @Autowired constructor(
    private val client: WebTestClient
) {
    companion object {
        private const val token =
            "eyJhbGciOiJIUzUxMiJ9.eyJhY2NvdW50X2lkIjoiYmRhMGY4ZjAzZjhlMTFlZGI4NzgwMjQyYWMxMjAwMDIiLCJST0xFIjpbIkNQQy1TSVRFLUFETUlOIiwiTFJDLVNJVEUtQ" +
                "URNSU4iLCJTVVBFUi1BRE1JTiJdLCJ1c2VyX2lkIjoiam1jQGJpdGh1bWJzeXN0ZW1zLmNvbSIsImlzcyI6ImptY0BiaXRodW1ic3lzdGVtcy5jb20iLCJzdWIiOiI2MmE" +
                "xNWY0YWU0MTI5YjUxOGIxMzMxMjgiLCJpYXQiOjE2Njk5NzQwMTIsImp0aSI6ImYwZDY3ZWQ0LTRkZGMtNDJlMi05NTg0LWI1OWE1ZGViYzZiMyIsImV4cCI6OTI0NjQ0N" +
                "zAwMDB9.7M9HsUH9K4qg-c4RKrohujmR83InWr1UJr2kZkcLKD_Sp1I4IoByaCF-yaBB9i3i8KAQryVTlC9OxkA4TCnyEg"
        private lateinit var id: String
    }

    @Test
    @Order(1)
    fun `카테고리 등록 테스트`() {
        val body = NoticeCategoryRequest(name = "카테고리", isUse = false)

        val responseBody: Response<*>? = client.post()
            .uri("/api/v1/mng/cms/notices/categories")
            .header("authorization", "Bearer $token")
            .body(Mono.just(body), NoticeCategoryRequest::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody(Response::class.java)
            .returnResult().responseBody

        println(responseBody)
        id = responseBody?.data.toString().substringAfter("=").substringBefore(",")
        responseBody?.result `should be` ResponseCode.SUCCESS
    }

    @Test
    @Order(2)
    fun `카테고리 목록 조회 테스트`() {
        val responseBody: Response<*>? = client.get()
            .uri("/api/v1/mng/cms/notices/categories")
            .exchange()
            .expectStatus().isOk
            .expectBody(Response::class.java)
            .returnResult().responseBody

        println(responseBody)
        responseBody?.result `should be` ResponseCode.SUCCESS
        responseBody?.data?.toString()?.isNotEmpty() `should be` true
    }

    @Test
    @Order(3)
    fun `카테고리 조회 테스트`() {
        val responseBody: Response<*>? = client.get()
            .uri("/api/v1/mng/cms/notices/categories/$id}")
            .exchange()
            .expectStatus().isOk
            .expectBody(Response::class.java)
            .returnResult().responseBody

        println(responseBody)
        responseBody?.result `should be` ResponseCode.SUCCESS
    }

    @Test
    @Order(4)
    fun `카테고리 수정 테스트`() {
        val body = NoticeCategoryRequest(name = "카테고리", isUse = true, isDelete = true)

        val responseBody: Response<*>? = client.put()
            .uri("/api/v1/mng/cms/notices/categories/$id}")
            .header("authorization", "Bearer $token")
            .body(Mono.just(body), NoticeCategoryRequest::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody(Response::class.java)
            .returnResult().responseBody

        println(responseBody)
        responseBody?.result `should be` ResponseCode.SUCCESS
    }

    @Test
    @Order(5)
    fun `카테고리 삭제 테스트`() {
        val responseBody: Response<*>? = client.delete()
            .uri("/api/v1/mng/cms/notices/categories/$id}")
            .header("authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody(Response::class.java)
            .returnResult().responseBody

        println(responseBody)
        responseBody?.result `should be` ResponseCode.SUCCESS
    }
}
