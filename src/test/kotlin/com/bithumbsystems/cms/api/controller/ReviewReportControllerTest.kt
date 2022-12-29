package com.bithumbsystems.cms.api.controller

import com.bithumbsystems.cms.api.model.enums.ResponseCode
import com.bithumbsystems.cms.api.model.request.ReviewReportRequest
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

class ReviewReportControllerTest @Autowired constructor(
    private val client: WebTestClient
) : CommonControllerTest() {

    @Test
    @Order(1)
    fun `가상자산 검토보고서 등록 테스트`() {
        val body = ReviewReportRequest(title = "가상자산 제목", content = "가상자산 본문")
        val bodyBuilder: MultipartBodyBuilder = MultipartBodyBuilder().apply {
            this.asyncPart("request", Mono.just(body), ReviewReportRequest::class.java)
        }

        val responseBody: Response<*>? = client.post()
            .uri("/api/v1/mng/cms/review-reports")
            .header("authorization", "Bearer $token")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
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
    fun `가상자산 검토보고서 목록 조회 테스트`() {
        val responseBody: Response<*>? = client.get()
            .uri("/api/v1/mng/cms/review-reports")
            .header("authorization", "Bearer $token")
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
    fun `가상자산 검토보고서 조회 테스트`() {
        val responseBody: Response<*>? = client.get()
            .uri("/api/v1/mng/cms/review-reports/$id}")
            .exchange()
            .expectStatus().isOk
            .expectBody(Response::class.java)
            .returnResult().responseBody

        println(responseBody)
        responseBody?.result `should be` ResponseCode.SUCCESS
    }

    @Test
    @Order(4)
    fun `가상자산 검토보고서 수정 테스트`() {
        val body = ReviewReportRequest(title = "가상자산 제목2", content = "가상자산 본문2")
        val bodyBuilder: MultipartBodyBuilder = MultipartBodyBuilder().apply {
            this.asyncPart("request", Mono.just(body), ReviewReportRequest::class.java)
        }

        val responseBody: Response<*>? = client.put()
            .uri("/api/v1/mng/cms/review-reports/$id}")
            .header("authorization", "Bearer $token")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .exchange()
            .expectStatus().isOk
            .expectBody(Response::class.java)
            .returnResult().responseBody

        println(responseBody)
        responseBody?.result `should be` ResponseCode.SUCCESS
    }

    @Test
    @Order(5)
    fun `가상자산 검토보고서 삭제 테스트`() {
        val responseBody: Response<*>? = client.delete()
            .uri("/api/v1/mng/cms/review-reports/$id}")
            .header("authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody(Response::class.java)
            .returnResult().responseBody

        println(responseBody)
        responseBody?.result `should be` ResponseCode.SUCCESS
    }
}
