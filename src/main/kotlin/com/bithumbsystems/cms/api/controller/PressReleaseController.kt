package com.bithumbsystems.cms.api.controller

import com.bithumbsystems.cms.api.config.operator.ServiceOperator.execute
import com.bithumbsystems.cms.api.config.resolver.Account
import com.bithumbsystems.cms.api.config.resolver.CurrentUser
import com.bithumbsystems.cms.api.config.resolver.QueryParam
import com.bithumbsystems.cms.api.model.request.FileRequest
import com.bithumbsystems.cms.api.model.request.PressReleaseRequest
import com.bithumbsystems.cms.api.model.request.SearchParams
import com.bithumbsystems.cms.api.model.response.PressReleaseDetailResponse
import com.bithumbsystems.cms.api.model.response.PressReleaseResponse
import com.bithumbsystems.cms.api.service.PressReleaseService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Encoding
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/press-releases")
class PressReleaseController(
    private val pressReleaseService: PressReleaseService
) {
    /**
     * 보도자료 생성
     * @param request 생성할 보도자료 데이터
     * @param account 생성자 계정 정보
     */
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(
        summary = "보도자료 생성",
        description = "보도자료를 생성합니다.",
        tags = ["보도자료 > 보도자료 관리"],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "생성 성공",
                content = [Content(schema = Schema(implementation = PressReleaseDetailResponse::class))]
            )
        ]
    )
    suspend fun createPressRelease(
        @RequestPart("request")
        @Parameter(content = [Content(encoding = [Encoding(name = "request", contentType = MediaType.APPLICATION_JSON_VALUE)])])
        request: PressReleaseRequest,
        @RequestPart(value = "file", required = false) @Parameter(
            description = "첨부 파일",
            content = [Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)]
        )
        filePart: FilePart?,
        @RequestPart(value = "share_file", required = false) @Parameter(
            description = "공유 태그 이미지",
            content = [Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)]
        )
        shareFilePart: FilePart?,
        @Parameter(hidden = true) @CurrentUser
        account: Account
    ) = execute {
        pressReleaseService.createPressRelease(
            request = request,
            fileRequest = FileRequest(file = filePart, shareFile = shareFilePart),
            account = account
        )
    }

    @GetMapping
    @Operation(
        summary = "보도자료 목록 조회",
        description = "보도자료 목록을 조회합니다.",
        tags = ["보도자료 > 보도자료 관리"],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "생성 성공",
                content = [Content(schema = Schema(implementation = PressReleaseResponse::class))]
            )
        ],
        parameters = [
            Parameter(
                description = "제목/내용",
                name = "query",
                `in` = ParameterIn.QUERY,
                schema = Schema(implementation = String::class),
                example = "검색어"
            ),
            Parameter(
                description = "상태",
                name = "is_show",
                `in` = ParameterIn.QUERY,
                schema = Schema(implementation = Boolean::class),
                example = "true"
            ),
            Parameter(
                description = "등록기간(시작일)",
                name = "start_date",
                `in` = ParameterIn.QUERY,
                schema = Schema(implementation = LocalDate::class),
                example = "2022-12-31"
            ),
            Parameter(
                description = "등록기간(종료일)",
                name = "end_date",
                `in` = ParameterIn.QUERY,
                schema = Schema(implementation = LocalDate::class),
                example = "2022-12-31"
            )
        ]
    )
    suspend fun getPressReleases(
        @QueryParam
        @Parameter(hidden = true)
        searchParams: SearchParams,
        @Parameter(hidden = true) @CurrentUser
        account: Account
    ) = execute {
        pressReleaseService.getPressReleases(searchParams = searchParams, account = account)
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "보도자료 조회",
        description = "보도자료 조회합니다.",
        tags = ["보도자료 > 보도자료 관리"],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(schema = Schema(implementation = PressReleaseResponse::class))]
            )
        ]
    )
    suspend fun getPressRelease(@PathVariable id: String) = execute {
        pressReleaseService.getPressRelease(id)
    }

    @PutMapping("/{id}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(
        summary = "보도자료 수정",
        description = "보도자료를 수정합니다.",
        tags = ["보도자료 > 보도자료 관리"],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "수정 성공",
                content = [Content(schema = Schema(implementation = PressReleaseResponse::class))]
            )
        ]
    )
    suspend fun updatePressRelease(
        @PathVariable id: String,
        @RequestPart("request")
        @Parameter(content = [Content(encoding = [Encoding(name = "request", contentType = MediaType.APPLICATION_JSON_VALUE)])])
        request: PressReleaseRequest,
        @RequestPart(value = "file", required = false) @Parameter(
            description = "첨부 파일",
            content = [Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)]
        )
        filePart: FilePart?,
        @RequestPart(value = "share_file", required = false) @Parameter(
            description = "공유 태그 이미지",
            content = [Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)]
        )
        shareFilePart: FilePart?,
        @Parameter(hidden = true) @CurrentUser
        account: Account
    ) = execute {
        pressReleaseService.updatePressRelease(
            id = id,
            request = request,
            fileRequest = FileRequest(file = filePart, shareFile = shareFilePart),
            account = account
        )
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "보도자료 삭제",
        description = "보도자료를 삭제합니다.",
        tags = ["보도자료 > 보도자료 관리"],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "삭제 성공",
                content = [Content(schema = Schema(implementation = PressReleaseResponse::class))]
            )
        ]
    )
    suspend fun deletePressRelease(
        @PathVariable id: String,
        @Parameter(hidden = true) @CurrentUser
        account: Account
    ) = execute {
        pressReleaseService.deletePressRelease(id = id, account = account)
    }
}
