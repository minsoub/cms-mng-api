package com.bithumbsystems.cms.api.controller

import com.bithumbsystems.cms.api.config.operator.ServiceOperator.execute
import com.bithumbsystems.cms.api.config.resolver.Account
import com.bithumbsystems.cms.api.config.resolver.CurrentUser
import com.bithumbsystems.cms.api.config.resolver.QueryParam
import com.bithumbsystems.cms.api.model.enums.SortBy
import com.bithumbsystems.cms.api.model.enums.SortDirection
import com.bithumbsystems.cms.api.model.request.*
import com.bithumbsystems.cms.api.model.response.ReviewReportDetailResponse
import com.bithumbsystems.cms.api.model.response.ReviewReportResponse
import com.bithumbsystems.cms.api.service.ReviewReportService
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
@RequestMapping("/review-reports")
class ReviewReportController(
    private val reviewReportService: ReviewReportService
) {
    /**
     * 가상자산 검토보고서 생성
     * @param request 생성할 가상자산 검토보고서 데이터
     * @param account 생성자 계정 정보
     */
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(
        summary = "가상자산 검토보고서 생성",
        description = "가상자산 검토보고서를 생성합니다.",
        tags = ["가상자산 검토보고서 > 가상자산 검토보고서 관리"],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "생성 성공",
                content = [Content(schema = Schema(implementation = ReviewReportDetailResponse::class))]
            )
        ]
    )
    suspend fun createReviewReport(
        @RequestPart("request")
        @Parameter(content = [Content(encoding = [Encoding(name = "request", contentType = MediaType.APPLICATION_JSON_VALUE)])])
        request: ReviewReportRequest,
        @RequestPart(value = "file", required = false) @Parameter(
            description = "첨부 파일",
            content = [Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)]
        )
        filePart: FilePart,
        @RequestPart(value = "share_file", required = false) @Parameter(
            description = "공유 태그 이미지",
            content = [Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)]
        )
        shareFilePart: FilePart,
        @RequestPart(value = "thumbnail_file", required = false) @Parameter(
            description = "썸네일 이미지",
            content = [Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)]
        )
        thumbnailFilePart: FilePart,
        @Parameter(hidden = true) @CurrentUser
        account: Account
    ) = execute {
        reviewReportService.createReviewReport(
            request = request,
            fileRequest = FileRequest(file = filePart, shareFile = shareFilePart, thumbnailFile = thumbnailFilePart),
            account = account
        )
    }

    @GetMapping
    @Operation(
        summary = "가상자산 검토보고서 목록 조회",
        description = "가상자산 검토보고서 목록을 조회합니다.",
        tags = ["가상자산 검토보고서 > 가상자산 검토보고서 관리"],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "생성 성공",
                content = [Content(schema = Schema(implementation = ReviewReportResponse::class))]
            )
        ],
        parameters = [
            Parameter(
                description = "이동할 페이지",
                name = "page",
                `in` = ParameterIn.QUERY,
                schema = Schema(implementation = Int::class),
                example = "0"
            ),
            Parameter(
                description = "페이지 사이즈",
                name = "page_size",
                `in` = ParameterIn.QUERY,
                schema = Schema(implementation = Int::class),
                example = "15"
            ),
            Parameter(
                description = "검색어",
                name = "query",
                `in` = ParameterIn.QUERY,
                schema = Schema(implementation = String::class),
                example = "검색어"
            ),
            Parameter(
                description = "카테고리 아이디",
                name = "category_id",
                `in` = ParameterIn.QUERY,
                schema = Schema(implementation = String::class),
                example = "5315d045f031424a8ca53128f344ac04"
            ),
            Parameter(
                description = "배너 여부",
                name = "is_banner",
                `in` = ParameterIn.QUERY,
                schema = Schema(implementation = Boolean::class),
                example = "false"
            ),
            Parameter(
                description = "사용 여부",
                name = "is_show",
                `in` = ParameterIn.QUERY,
                schema = Schema(implementation = Boolean::class),
                example = "true"
            ),
            Parameter(
                description = "상태(카테고리)",
                name = "is_use",
                `in` = ParameterIn.QUERY,
                schema = Schema(implementation = Boolean::class),
                example = "true"
            ),
            Parameter(
                description = "정렬 타겟",
                name = "sort_by",
                `in` = ParameterIn.QUERY,
                schema = Schema(implementation = SortBy::class),
                example = "SCREEN_DATE"
            ),
            Parameter(
                description = "정렬 방향",
                name = "sort_direction",
                `in` = ParameterIn.QUERY,
                schema = Schema(implementation = SortDirection::class),
                example = "ASC"
            ),
            Parameter(
                description = "검색 시작일",
                name = "start_date",
                `in` = ParameterIn.QUERY,
                schema = Schema(implementation = LocalDate::class),
                example = "2022-12-31"
            ),
            Parameter(
                description = "검색 종료일",
                name = "end_date",
                `in` = ParameterIn.QUERY,
                schema = Schema(implementation = LocalDate::class),
                example = "2022-12-31"
            )
        ]
    )
    suspend fun getReviewReports(
        @QueryParam
        @Parameter(hidden = true)
        searchParams: SearchParams
    ) = execute {
        reviewReportService.getReviewReports(searchParams)
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "가상자산 검토보고서 조회",
        description = "가상자산 검토보고서 조회합니다.",
        tags = ["가상자산 검토보고서 > 가상자산 검토보고서 관리"],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(schema = Schema(implementation = ReviewReportResponse::class))]
            )
        ]
    )
    suspend fun getReviewReport(@PathVariable id: String) = execute {
        reviewReportService.getReviewReport(id)
    }

    @PutMapping("/{id}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(
        summary = "가상자산 검토보고서 수정",
        description = "가상자산 검토보고서를 수정합니다.",
        tags = ["가상자산 검토보고서 > 가상자산 검토보고서 관리"],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "수정 성공",
                content = [Content(schema = Schema(implementation = ReviewReportResponse::class))]
            )
        ]
    )
    suspend fun updateReviewReport(
        @PathVariable id: String,
        @RequestPart("request")
        @Parameter(content = [Content(encoding = [Encoding(name = "request", contentType = MediaType.APPLICATION_JSON_VALUE)])])
        request: ReviewReportRequest,
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
        @RequestPart(value = "thumbnail_file", required = false) @Parameter(
            description = "썸네일 이미지",
            content = [Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)]
        )
        thumbnailFilePart: FilePart?,
        @Parameter(hidden = true) @CurrentUser
        account: Account
    ) = execute {
        reviewReportService.updateReviewReport(
            id = id,
            request = request,
            fileRequest = FileRequest(file = filePart, shareFile = shareFilePart, thumbnailFile = thumbnailFilePart),
            account = account
        )
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "가상자산 검토보고서 삭제",
        description = "가상자산 검토보고서를 삭제합니다.",
        tags = ["가상자산 검토보고서 > 가상자산 검토보고서 관리"],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "삭제 성공",
                content = [Content(schema = Schema(implementation = ReviewReportResponse::class))]
            )
        ]
    )
    suspend fun deleteReviewReport(
        @PathVariable id: String,
        @Parameter(hidden = true) @CurrentUser
        account: Account
    ) = execute {
        reviewReportService.deleteReviewReport(id = id, account = account)
    }
}
