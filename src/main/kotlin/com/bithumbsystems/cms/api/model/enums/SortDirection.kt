package com.bithumbsystems.cms.api.model.enums

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "정렬 방법 Enum")
enum class SortDirection {
    @Schema(description = "오름차순")
    ASC,

    @Schema(description = "내림차순")
    DESC
}
