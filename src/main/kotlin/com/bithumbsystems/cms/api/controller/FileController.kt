package com.bithumbsystems.cms.api.controller

import com.bithumbsystems.cms.api.config.operator.ServiceOperator.execute
import com.bithumbsystems.cms.api.config.resolver.Account
import com.bithumbsystems.cms.api.config.resolver.CurrentUser
import com.bithumbsystems.cms.api.model.response.FileInfoResponse
import com.bithumbsystems.cms.api.model.response.Response
import com.bithumbsystems.cms.api.service.FileService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import software.amazon.awssdk.core.async.SdkPublisher
import java.nio.ByteBuffer

@RestController
@RequestMapping("/files")
class FileController(
    private val fileService: FileService
) {
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(
        summary = "파일 업로드",
        description = "파일을 업로드 합니다.",
        tags = ["게시글 > 등록/수정"],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "업로드 성공",
                content = [Content(schema = Schema(implementation = FileInfoResponse::class))]
            )
        ]
    )
    suspend fun fileUpload(
        @RequestPart(name = "file") @Parameter(
            description = "File to be uploaded",
            content = [Content(mediaType = APPLICATION_OCTET_STREAM_VALUE)]
        )
        file: FilePart,
        @Parameter(hidden = true) @CurrentUser
        account: Account
    ): ResponseEntity<Response<Any>> = execute {
        fileService.addFileInfo(file = file, account = account)
    }

    @GetMapping("/{fileKey}", produces = [APPLICATION_OCTET_STREAM_VALUE])
    @Operation(
        summary = "파일 다운로드",
        description = "파일을 다운로드합니다.",
        tags = ["게시글 > 상세 조회"],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "다운로드 성공",
                content = [Content(schema = Schema(implementation = ByteBuffer::class))]
            )
        ]
    )
    fun fileDownload(@PathVariable fileKey: String): Mono<ResponseEntity<SdkPublisher<ByteBuffer>>> {
        return fileService.download(fileKey)
            .flatMap {
                Mono.just(
                    ResponseEntity
                        .status(200)
                        .header(HttpHeaders.CONTENT_TYPE, it.result.response().contentType())
                        .header(HttpHeaders.CONTENT_LENGTH, it.result.response().contentLength().toString())
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=${it.fileName}")
                        .body(it.result.map { bf -> bf })
                )
            }
    }
}
