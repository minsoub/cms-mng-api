package com.bithumbsystems.cms.api.model.enums

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "레디스 조회 키 Enum")
enum class RedisKeys {
    @Schema(description = "공지사항 고정 게시글")
    CMS_NOTICE_FIX,

    @Schema(description = "보도자료 고정 게시글")
    CMS_PRESS_RELEASE_FIX,

    @Schema(description = "이벤트 고정 게시글")
    CMS_EVENT_FIX,

    @Schema(description = "가상 자산 고정 게시글")
    CMS_REVIEW_REPORT_FIX,

    @Schema(description = "투자유의 고정 게시글")
    CMS_INVESTMENT_WARNING_FIX,

    @Schema(description = "경제연구소 고정 게시글")
    CMS_ECONOMIC_RESEARCH_FIX,

    @Schema(description = "공지사항 카테고리")
    CMS_NOTICE_CATEGORY,

    @Schema(description = "메인 페이지 공지사항")
    CMS_NOTICE_RECENT,

    @Schema(description = "메인페이지 보도자료")
    CMS_PRESS_RELEASE_RECENT,

    @Schema(description = "공지사항 배너")
    CMS_NOTICE_BANNER,

    @Schema(description = "게시판 조회수")
    CMS_READ_COUNT,

    @Schema(description = "RMapCacheReactive 테스트용 키")
    MAP_TEST_KEY,

    @Schema(description = "RListReactive 테스트용 키")
    LIST_TEST_KEY,

    @Schema(description = "ScoredSortedSetReactive 테스트용 키")
    SORTED_SET_TEST_KEY,

    @Schema(description = "RBucket 테스트용 키")
    BUCKET_TEST_KEY,
}
