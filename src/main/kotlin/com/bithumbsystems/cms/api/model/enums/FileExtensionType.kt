package com.bithumbsystems.cms.api.model.enums

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "파일 확장자 Enum")
enum class FileExtensionType {
    JPG, JPEG, GIF, PNG, PDF
}
