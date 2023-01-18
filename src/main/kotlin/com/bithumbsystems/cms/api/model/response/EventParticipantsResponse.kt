package com.bithumbsystems.cms.api.model.response

import com.bithumbsystems.cms.api.util.EncryptionUtil.decryptAES
import com.bithumbsystems.cms.persistence.mongo.entity.CmsEventParticipants
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "이벤트 참여자 목록 응답")
class EventParticipantsResponse(
    @Schema(description = "이벤트 아이디", example = "b40f760a9ce84702905347b1e0d98aeb")
    val eventId: String,

    @Schema(description = "UID")
    val uid: String,

    @Schema(description = "개인정보 수집 및 이용 동의 여부", example = "true")
    val isAgree: Boolean? = true,

    @Schema(description = "참여일시", example = "2022-12-07 11:11:11")
    val createDate: LocalDateTime?,

    @Schema(description = "이벤트 종료 일시", example = "2022-12-07 11:11:11")
    val eventEndDate: LocalDateTime?
)

/**
 * CmsEvent Entity를 EventParticipantsResponse로 변환한다.
 * @return EventParticipantsResponse
 */
fun CmsEventParticipants.toResponse(password: String): EventParticipantsResponse = EventParticipantsResponse(
    eventId = eventId,
    uid = uid.decryptAES(password),
    isAgree = isAgree,
    createDate = createDate,
    eventEndDate = eventEndDate
)
