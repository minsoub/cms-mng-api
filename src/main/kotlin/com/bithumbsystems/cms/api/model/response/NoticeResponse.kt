package com.bithumbsystems.cms.api.model.response

import com.bithumbsystems.cms.api.util.MaskingUtil.getEmailMask
import com.bithumbsystems.cms.persistence.mongo.entity.CmsNotice
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "공지사항 목록 응답")
class NoticeResponse(
    @Schema(description = "아이디", example = "b40f760a9ce84702905347b1e0d98aeb")
    val id: String,
    @Schema(description = "카테고리명")
    val categoryId: List<String>,
    @Schema(description = "제목", example = "제목")
    val title: String,
    @Schema(description = "상단 고정 여부", example = "false")
    val isFixTop: Boolean = false,
    @Schema(description = "배너 여부", example = "false")
    val isBanner: Boolean = false,
    @Schema(description = "상태(공개 여부)", example = "false")
    val isShow: Boolean = true,
    @Schema(description = "초안 여부", example = "false")
    var isDraft: Boolean = false,
    @Schema(description = "조회수", example = "0")
    var readCount: Long = 0,
    @Schema(description = "화면 표시용 일시", example = "2022-12-07 11:11:11")
    var screenDate: LocalDateTime? = null,
    @Schema(description = "생성자 이메일", example = "abc@example.com")
    val createAccountEmail: String,
    @Schema(description = "생성일시, 예시: 2022-12-07 11:11:11", example = "2022-12-07 11:11:11")
    val createDate: LocalDateTime,
    @Schema(description = "수정일시, 예시: 2022-12-07 11:11:11", example = "2022-12-07 11:11:11")
    val updateDate: LocalDateTime? = null
)

/**
 * CmsNotice Entity를 NoticeCategoryDetailResponse 변환한다.
 * @return 마스킹 처리된 응답
 */
fun CmsNotice.toMaskingResponse() = NoticeResponse(
    id = id,
    categoryId = categoryId,
    title = title,
    isFixTop = isFixTop,
    isBanner = isBanner,
    isShow = isShow,
    isDraft = isDraft,
    readCount = readCount,
    screenDate = screenDate,
    createAccountEmail = createAccountEmail.getEmailMask(),
    createDate = createDate,
    updateDate = updateDate
)
