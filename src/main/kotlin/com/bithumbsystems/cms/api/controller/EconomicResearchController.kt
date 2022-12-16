package com.bithumbsystems.cms.api.controller

import com.bithumbsystems.cms.api.config.operator.ServiceOperator.execute
import com.bithumbsystems.cms.api.config.resolver.Account
import com.bithumbsystems.cms.api.config.resolver.CurrentUser
import com.bithumbsystems.cms.api.config.resolver.QueryParam
import com.bithumbsystems.cms.api.model.enums.SortBy
import com.bithumbsystems.cms.api.model.enums.SortDirection
import com.bithumbsystems.cms.api.model.request.EconomicResearchRequest
import com.bithumbsystems.cms.api.model.request.FileRequest
import com.bithumbsystems.cms.api.model.request.SearchParams
import com.bithumbsystems.cms.api.model.response.EconomicResearchDetailResponse
import com.bithumbsystems.cms.api.model.response.EconomicResearchResponse
import com.bithumbsystems.cms.api.service.EconomicResearchService
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
@RequestMapping("/economic-researches")
class EconomicResearchController(
    private val economicResearchService: EconomicResearchService
) {
    /**
     * 빗썸 경제연구소 생성
     * @param request 생성할 빗썸 경제연구소 데이터
     * @param account 생성자 계정 정보
     */
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(
        summary = "빗썸 경제연구소 생성",
        description = "빗썸 경제연구소를 생성합니다.",
        tags = ["빗썸 경제연구소 > 빗썸 경제연구소 관리"],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "생성 성공",
                content = [Content(schema = Schema(implementation = EconomicResearchDetailResponse::class))]
            )
        ]
    )
    suspend fun createEconomicResearch(
        @RequestPart("request")
        @Parameter(content = [Content(encoding = [Encoding(name = "request", contentType = MediaType.APPLICATION_JSON_VALUE)])])
        request: EconomicResearchRequest,
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
        economicResearchService.createEconomicResearch(
            request = request,
            fileRequest = FileRequest(file = filePart, shareFile = shareFilePart, thumbnailFile = thumbnailFilePart),
            account = account
        )
    }

    @GetMapping
    @Operation(
        summary = "빗썸 경제연구소 목록 조회",
        description = "빗썸 경제연구소 목록을 조회합니다.",
        tags = ["빗썸 경제연구소 > 빗썸 경제연구소 관리"],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "생성 성공",
                content = [Content(schema = Schema(implementation = EconomicResearchResponse::class))]
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
    suspend fun getEconomicResearches(
        @QueryParam
        @Parameter(hidden = true)
        searchParams: SearchParams
    ) = execute {
        economicResearchService.getEconomicResearches(searchParams)
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "빗썸 경제연구소 조회",
        description = "빗썸 경제연구소 조회합니다.",
        tags = ["빗썸 경제연구소 > 빗썸 경제연구소 관리"],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(schema = Schema(implementation = EconomicResearchResponse::class))]
            )
        ]
    )
    suspend fun getEconomicResearch(@PathVariable id: String) = execute {
        economicResearchService.getEconomicResearch(id)
    }

    @PutMapping("/{id}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(
        summary = "빗썸 경제연구소 수정",
        description = "빗썸 경제연구소를 수정합니다.",
        tags = ["빗썸 경제연구소 > 빗썸 경제연구소 관리"],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "수정 성공",
                content = [Content(schema = Schema(implementation = EconomicResearchResponse::class))]
            )
        ]
    )
    suspend fun updateEconomicResearch(
        @PathVariable id: String,
        @RequestPart("request")
        @Parameter(content = [Content(encoding = [Encoding(name = "request", contentType = MediaType.APPLICATION_JSON_VALUE)])])
        request: EconomicResearchRequest,
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
        economicResearchService.updateEconomicResearch(
            id = id,
            request = request,
            fileRequest = FileRequest(file = filePart, shareFile = shareFilePart, thumbnailFile = thumbnailFilePart),
            account = account
        )
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "빗썸 경제연구소 삭제",
        description = "빗썸 경제연구소를 삭제합니다.",
        tags = ["빗썸 경제연구소 > 빗썸 경제연구소 관리"],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "삭제 성공",
                content = [Content(schema = Schema(implementation = EconomicResearchResponse::class))]
            )
        ]
    )
    suspend fun deleteEconomicResearch(
        @PathVariable id: String,
        @Parameter(hidden = true) @CurrentUser
        account: Account
    ) = execute {
        economicResearchService.deleteEconomicResearch(id = id, account = account)
    }
}
