package com.bithumbsystems.cms.api.model.request

import com.bithumbsystems.cms.api.config.resolver.Account
import com.bithumbsystems.cms.api.model.enums.FileExtensionType
import com.bithumbsystems.cms.persistence.mongo.entity.CmsFileInfo
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "파일 정보 요청")
class FileInfoRequest(
    @Schema(description = "아이디")
    val id: String,
    @Schema(description = "파일명")
    val name: String,
    @Schema(description = "파일 사이즈")
    val size: Long,
    @Schema(description = "파일 확장자")
    val extension: FileExtensionType
) {
    @Schema(description = "생성자 아이디", hidden = true)
    var createAccountId: String = ""

    @Schema(description = "생성자 이메일", hidden = true)
    var createAccountEmail: String = ""
}

fun FileInfoRequest.setCreateInfo(account: Account) = apply {
    this.createAccountId = account.accountId
    this.createAccountEmail = account.email
}

fun FileInfoRequest.toEntity() = CmsFileInfo(
    id = id,
    name = name,
    size = size,
    extension = extension,
    createAccountId = createAccountId,
    createAccountEmail = createAccountEmail
)
