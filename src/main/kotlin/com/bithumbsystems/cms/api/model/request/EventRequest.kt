package com.bithumbsystems.cms.api.model.request

import com.bithumbsystems.cms.api.model.constants.ShareConstants.EVENT_TITLE
import com.bithumbsystems.cms.api.model.enums.EventTarget
import com.bithumbsystems.cms.api.model.enums.EventType
import com.bithumbsystems.cms.persistence.mongo.entity.CmsEvent
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import javax.validation.Valid
import javax.validation.constraints.Future
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Schema(description = "이벤트 요청")
class EventRequest(
    @Schema(description = "제목", example = "제목", required = true, maxLength = 100)
    @field:Size(max = 100)
    override val title: String,
    @Schema(description = "본문", example = "본문", required = true)
    @field:NotBlank
    override val content: String
) : CommonBoardRequest(title = title, content = content) {
    @Schema(description = "이벤트 유형", allowableValues = ["DEFAULT", "PARTICIPATION", "LINK"])
    var type: EventType = EventType.DEFAULT

    @Schema(description = "참여 대상", allowableValues = ["LOGIN", "NOT_LOGIN"], required = false)
    var target: EventTarget? = null

    @Schema(description = "이벤트 시작일", example = "2023-01-13 17:09")
    @field:Future
    var eventStartDate: LocalDateTime? = null

    @Schema(description = "이벤트 종료일", example = "2023-01-13 17:09")
    @field:Future
    var eventEndDate: LocalDateTime? = null

    @Schema(description = "개인정보 수집 및 이용 동의 문구", example = "개인정보 수집 및 이용 동의 문구")
    var agreementContent: String? = null

    @Schema(description = "버튼명", example = "버튼명", maxLength = 10)
    @field:Size(max = 10)
    var buttonName: String? = null

    @Schema(description = "버튼 색상", example = "#000000", maxLength = 10)
    @field:Size(max = 10)
    var buttonColor: String? = null

    @Schema(description = "버튼 경로", example = "https://www.bithumb.com")
    var buttonUrl: String? = null

    @Schema(description = "메시지")
    @field:Valid
    var message: Message? = null

    internal val isValid = eventStartDate?.isBefore(LocalDateTime.now()) == true || eventEndDate?.isBefore(LocalDateTime.now()) == true ||
        (buttonName?.length ?: 0) > 10 || (buttonColor?.length ?: 0) > 10 || (message?.participateMessage?.length ?: 0) > 20 ||
        (message?.duplicateMessage?.length ?: 0) > 20
}

@Schema(description = "이벤트 메시지")
class Message(
    @Schema(description = "참여 완료 시 메시지", example = "참여가 완료되었습니다.", maxLength = 20)
    @field:Size(max = 20)
    val participateMessage: String,
    @Schema(description = "중복 참여 시 메시지", example = "이미 참여한 이벤트입니다.", maxLength = 20)
    @field:Size(max = 20)
    val duplicateMessage: String
)

fun EventRequest.toEntity(): CmsEvent {
    val entity = CmsEvent(
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
    entity.shareButtonName = shareButtonName ?: EVENT_TITLE
    entity.isSchedule = isSchedule
    entity.scheduleDate = scheduleDate
    entity.isDraft = isDraft
    entity.readCount = readCount
    entity.type = type
    entity.target = target
    entity.eventStartDate = eventStartDate
    entity.eventEndDate = eventEndDate
    entity.agreementContent = agreementContent
    entity.buttonName = buttonName
    entity.buttonColor = buttonColor
    entity.buttonUrl = buttonUrl
    entity.message = message
    entity.isUseUpdateDate = isUseUpdateDate
    entity.isAlignTop = isAlignTop
    entity.screenDate = screenDate ?: entity.createDate
    entity.updateAccountId = updateAccountId
    entity.updateAccountEmail = updateAccountEmail
    entity.updateDate = updateDate

    return entity
}

fun EventRequest.validateEvent(): Boolean {
    return when {
        isValid -> {
            false
        }

        else -> {
            true
        }
    }
}
