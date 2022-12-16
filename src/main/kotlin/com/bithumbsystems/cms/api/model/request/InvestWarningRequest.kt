package com.bithumbsystems.cms.api.model.request

import com.bithumbsystems.cms.api.model.constants.ShareConstants.INVEST_WARNING_TITLE
import com.bithumbsystems.cms.persistence.mongo.entity.CmsInvestWarning
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Schema(description = "투자유의 요청")
class InvestWarningRequest(
    @Schema(description = "제목", example = "제목", required = true, maxLength = 100)
    @field:Size(max = 100)
    val title: String,
    @Schema(description = "본문", example = "본문", required = true)
    @field:NotBlank
    val content: String
) : CommonBoardRequest()

fun InvestWarningRequest.toEntity(): CmsInvestWarning {
    val entity = CmsInvestWarning(
        title = title,
        content = content,
        createAccountId = createAccountId,
        createAccountEmail = createAccountEmail
    )

    entity.isFixTop = isFixTop
    entity.isShow = isShow
    entity.isDelete = isDelete
    entity.fileId = fileId
    entity.shareTitle = shareTitle ?: title
    entity.shareDescription = shareDescription
    entity.shareFileId = shareFileId
    entity.shareButtonName = shareButtonName ?: INVEST_WARNING_TITLE
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

fun InvestWarningRequest.validate(): Boolean {
    return when {
        title.length > 100 || content.isBlank() || (shareTitle?.length ?: 0) > 50 || (shareDescription?.length ?: 0) > 100 ||
            (shareButtonName?.length ?: 0) > 10 || (scheduleDate?.isBefore(LocalDateTime.now()) == true) -> {
            false
        }

        else -> {
            true
        }
    }
}
