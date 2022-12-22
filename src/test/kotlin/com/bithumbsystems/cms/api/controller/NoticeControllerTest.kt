package com.bithumbsystems.cms.api.controller

import com.bithumbsystems.cms.api.model.enums.ResponseCode
import com.bithumbsystems.cms.api.model.request.NoticeCategoryRequest
import com.bithumbsystems.cms.api.model.request.NoticeRequest
import com.bithumbsystems.cms.api.model.response.Response
import org.amshove.kluent.`should be`
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import reactor.core.publisher.Mono
import java.util.*

class NoticeControllerTest @Autowired constructor(
    private val client: WebTestClient
) : CommonControllerTest() {

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

        logger.info(responseBody.toString())
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

        logger.info(responseBody.toString())
        responseBody?.result `should be` ResponseCode.SUCCESS
        responseBody?.data?.toString()?.isNotEmpty() `should be` true
    }

    @Test
    @Order(3)
    fun `카테고리 조회 테스트`() {
        val responseBody: Response<*>? = client.get()
            .uri("/api/v1/mng/cms/notices/categories/$id")
            .exchange()
            .expectStatus().isOk
            .expectBody(Response::class.java)
            .returnResult().responseBody

        logger.info(responseBody.toString())
        responseBody?.result `should be` ResponseCode.SUCCESS
    }

    @Test
    @Order(4)
    fun `카테고리 수정 테스트`() {
        val body = NoticeCategoryRequest(name = "카테고리", isUse = true, isDelete = true)

        val responseBody: Response<*>? = client.put()
            .uri("/api/v1/mng/cms/notices/categories/$id")
            .header("authorization", "Bearer $token")
            .body(Mono.just(body), NoticeCategoryRequest::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody(Response::class.java)
            .returnResult().responseBody

        logger.info(responseBody.toString())
        responseBody?.result `should be` ResponseCode.SUCCESS
    }

    @Test
    @Order(5)
    fun `카테고리 삭제 테스트`() {
        val responseBody: Response<*>? = client.delete()
            .uri("/api/v1/mng/cms/notices/categories/$id")
            .header("authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody(Response::class.java)
            .returnResult().responseBody

        logger.info(responseBody.toString())
        responseBody?.result `should be` ResponseCode.SUCCESS
    }

    @Test
    @Order(6)
    fun `공지사항 등록 테스트`() {
        val body = NoticeRequest(title = "제목", content = "본문", categoryIds = listOf(UUID.randomUUID().toString().replace("-", "")))
        val bodyBuilder: MultipartBodyBuilder = MultipartBodyBuilder().apply {
            this.asyncPart("request", Mono.just(body), NoticeRequest::class.java)
        }

        val responseBody: Response<*>? = client.post()
            .uri("/api/v1/mng/cms/notices")
            .header("authorization", "Bearer $token")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .exchange()
            .expectStatus().isOk
            .expectBody(Response::class.java)
            .returnResult().responseBody

        logger.info(responseBody.toString())
        id = responseBody?.data.toString().substringAfter("=").substringBefore(",")
        responseBody?.result `should be` ResponseCode.SUCCESS
    }

    @Test
    @Order(7)
    fun `공지사항 목록 조회 테스트`() {
        val responseBody: Response<*>? = client.get()
            .uri("/api/v1/mng/cms/notices")
            .header("authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody(Response::class.java)
            .returnResult().responseBody

        logger.info(responseBody.toString())
        responseBody?.result `should be` ResponseCode.SUCCESS
        responseBody?.data?.toString()?.isNotEmpty() `should be` true
    }

    @Test
    @Order(8)
    fun `공지사항 조회 테스트`() {
        val responseBody: Response<*>? = client.get()
            .uri("/api/v1/mng/cms/notices/$id")
            .exchange()
            .expectStatus().isOk
            .expectBody(Response::class.java)
            .returnResult().responseBody

        logger.info(responseBody.toString())
        responseBody?.result `should be` ResponseCode.SUCCESS
    }

    @Test
    @Order(9)
    fun `공지사항 수정 테스트`() {
        val body = NoticeRequest(title = "제목2", content = "본문2", categoryIds = listOf(UUID.randomUUID().toString().replace("-", "")))
        val bodyBuilder: MultipartBodyBuilder = MultipartBodyBuilder().apply {
            this.asyncPart("request", Mono.just(body), NoticeRequest::class.java)
        }

        val responseBody: Response<*>? = client.put()
            .uri("/api/v1/mng/cms/notices/$id")
            .header("authorization", "Bearer $token")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .exchange()
            .expectStatus().isOk
            .expectBody(Response::class.java)
            .returnResult().responseBody

        logger.info(responseBody.toString())
        responseBody?.result `should be` ResponseCode.SUCCESS
    }

    @Test
    @Order(10)
    fun `공지사항 배너 등록 테스트`() {
        val responseBody: Response<*>? = client.post()
            .uri("/api/v1/mng/cms/notices/$id/banners")
            .header("authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody(Response::class.java)
            .returnResult().responseBody

        logger.info(responseBody.toString())
        responseBody?.result `should be` ResponseCode.SUCCESS
    }

    @Test
    @Order(11)
    fun `공지사항 배너 삭제 테스트`() {
        val responseBody: Response<*>? = client.delete()
            .uri("/api/v1/mng/cms/notices/$id/banners")
            .header("authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody(Response::class.java)
            .returnResult().responseBody

        logger.info(responseBody.toString())
        responseBody?.result `should be` ResponseCode.SUCCESS
    }

    @Test
    @Order(12)
    fun `공지사항 삭제 테스트`() {
        val responseBody: Response<*>? = client.delete()
            .uri("/api/v1/mng/cms/notices/$id")
            .header("authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody(Response::class.java)
            .returnResult().responseBody

        logger.info(responseBody.toString())
        responseBody?.result `should be` ResponseCode.SUCCESS
    }
}
