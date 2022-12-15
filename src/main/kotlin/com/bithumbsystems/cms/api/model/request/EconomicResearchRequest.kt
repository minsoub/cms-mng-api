package com.bithumbsystems.cms.api.model.request

import com.bithumbsystems.cms.api.model.constants.ShareConstants.ECONOMIC_RESEARCH_TITLE
import com.bithumbsystems.cms.persistence.mongo.entity.CmsEconomicResearch
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Schema(description = "경제연구소 요청")
class EconomicResearchRequest(
    @Schema(description = "제목", example = "제목", required = true, maxLength = 100)
    @field:Size(max = 100)
    override val title: String,
    @Schema(description = "본문", example = "본문", required = true)
    @field:NotBlank
    override val content: String
) : CommonBoardRequest(title = title, content = content) {
    @Schema(description = "썸네일 파일 아이디", example = "59f07bfd7490409b99c00b13bb50372e", hidden = true)
    var thumbnailFileId: String? = null

    @Schema(description = "썸네일 URL", example = "https://<bucket-name>.s3.amazonaws.com/<key>", hidden = true)
    var thumbnailUrl: String? = null
}

fun EconomicResearchRequest.toEntity(): CmsEconomicResearch {
    val entity = CmsEconomicResearch(
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
    entity.shareButtonName = shareButtonName ?: ECONOMIC_RESEARCH_TITLE
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
    entity.thumbnailFileId = thumbnailFileId
    entity.thumbnailUrl = thumbnailUrl

    return entity
}
