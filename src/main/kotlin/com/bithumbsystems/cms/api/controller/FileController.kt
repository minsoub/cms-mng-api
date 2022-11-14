package com.bithumbsystems.cms.api.controller

import com.bithumbsystems.cms.api.config.operator.ServiceOperator.execute
import com.bithumbsystems.cms.api.config.resolver.QueryParam
import com.bithumbsystems.cms.api.model.request.FileUploadRequest
import com.bithumbsystems.cms.api.model.response.Response
import com.bithumbsystems.cms.api.service.FileService
import com.bithumbsystems.cms.api.util.Logger
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/files")
class FileController(
    private val fileService: FileService
) {
    private val logger by Logger()

    @GetMapping
    @Operation(summary = "파일 업로드", description = "파일을 업로드 합니다.", tags = ["게시글 > 등록/수정"])
    suspend fun fileUpload(
        @QueryParam fileUploadRequest: FileUploadRequest
    ): ResponseEntity<Response<Any>> = execute {
        logger.info(fileUploadRequest.toString())
        fileService.upload()
    }

    @PostMapping
    @Operation(summary = "파일 다운로드", description = "파일을 다운로드합니다.", tags = ["게시글 > 상세 조회"])
    suspend fun fileDownload(): ResponseEntity<Response<Any>> = execute {
        fileService.download()
    }
}
