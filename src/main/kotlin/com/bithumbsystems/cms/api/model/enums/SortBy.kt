package com.bithumbsystems.cms.api.model.enums

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "정렬 대상 Enum")
enum class SortBy(val value: String) {
    @Schema(description = "기본 정렬(screen_date, create_date)")
    DEFAULT("screen_date,create_date"),

    @Schema(description = "화면 표시용 일자")
    SCREEN_DATE("screen_date"),

    @Schema(description = "생성 일시")
    CREATE_DATE("create_date"),

    @Schema(description = "수정 일시")
    UPDATE_DATE("update_date"),

    @Schema(description = "이름(카테고리명)")
    NAME("name"),

    @Schema(description = "제목")
    TITLE("title"),

    @Schema(description = "생성자 이메일", example = "abc@example.com")
    CREATE_ACCOUNT_EMAIL("create_account_email"),

    @Schema(description = "수정자 이메일", example = "abc@example.com")
    UPDATE_ACCOUNT_EMAIL("update_account_email"),
}
