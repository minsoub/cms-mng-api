package com.bithumbsystems.cms.api.model.request

import com.bithumbsystems.cms.api.model.constants.ShareConstants.NOTICE_TITLE
import com.bithumbsystems.cms.persistence.mongo.entity.CmsNotice
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Size

@Schema(description = "공지사항 요청")
data class NoticeRequest(
    @Schema(description = "카테고리 아이디 목록", example = "[\"5315d045f031424a8ca53128f344ac04\"]", required = true)
    @field:NotEmpty
    val categoryId: List<String>,
    @Schema(description = "제목", example = "제목", required = true, maxLength = 100)
    @field:Size(max = 100)
    override val title: String,
    @Schema(description = "본문", example = "본문", required = true)
    @field:NotBlank
    override val content: String
) : CommonBoardRequest(title = title, content = content) {
    @Schema(description = "카테고리 아이디 목록", allowableValues = ["true", "false"])
    var isBanner: Boolean = false
}

fun NoticeRequest.toEntity(): CmsNotice {
    val entity = CmsNotice(
        categoryId = categoryId,
        title = title,
        content = content,
        createAccountId = createAccountId,
        createAccountEmail = createAccountEmail
    )

    entity.categoryId = categoryId
    entity.isFixTop = isFixTop
    entity.isBanner = isBanner
    entity.isShow = isShow
    entity.isDelete = isDelete
    entity.fileId = fileId
    entity.shareTitle = shareTitle ?: title
    entity.shareDescription = shareDescription
    entity.shareFileId = shareFileId
    entity.shareButtonName = shareButtonName ?: NOTICE_TITLE
    entity.isSchedule = isSchedule
    entity.scheduleDate = scheduleDate
    entity.isDraft = isDraft
    entity.readCount = readCount
    entity.isUseUpdateDate = isUseUpdateDate
    entity.isAlignTop = isAlignTop
    entity.screenDate = screenDate
    entity.updateAccountId = updateAccountId
    entity.updateAccountEmail = updateAccountEmail
    entity.updateDate = updateDate

    return entity
}

fun NoticeRequest.validateNotice(): Boolean {
    return when {
        categoryId.isEmpty() || categoryId.size > 2 -> {
            false
        }

        else -> {
            true
        }
    }
}
