package com.bithumbsystems.cms.api.model.response

import com.bithumbsystems.cms.api.model.enums.FileExtensionType
import com.bithumbsystems.cms.api.util.EncryptionUtil.decryptAES
import com.bithumbsystems.cms.api.util.MaskingUtil.toEmailMask
import com.bithumbsystems.cms.persistence.mongo.entity.CmsFileInfo
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "파일 정보 응답")
class FileInfoResponse(
    @Schema(description = "아이디", example = "863b000fc2ae4e04b06cd1acbab34845")
    val id: String,
    @Schema(description = "파일명", example = "test")
    val name: String,
    @Schema(description = "파일 사이즈", example = "12345")
    val size: Long,
    @Schema(description = "파일 확장자", example = "JPG")
    val extension: FileExtensionType,
    @Schema(description = "생성자 아이디", example = "bda0f8f03f8e11edb8780242ac120002")
    val createAccountId: String,
    @Schema(description = "생성자 이메일", example = "abc@example.com")
    val createAccountEmail: String,
    @Schema(description = "생성일시, 예시: 2022-12-07 11:11:11", example = "2022-12-07 11:11:11")
    val createDate: LocalDateTime
)

/**
 * CmsFileInfo Entity를 FileInfoResponse로 변환한다.
 * @return FileInfoResponse
 */
fun CmsFileInfo.toResponse(password: String): FileInfoResponse = FileInfoResponse(
    id = id,
    name = name,
    size = size,
    extension = extension,
    createAccountId = createAccountId,
    createAccountEmail = createAccountEmail.decryptAES(password).toEmailMask(),
    createDate = createDate
)
