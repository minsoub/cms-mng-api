package com.bithumbsystems.cms.api.model.response

import com.bithumbsystems.cms.api.util.getS3Url
import com.bithumbsystems.cms.persistence.mongo.entity.CmsFileInfo
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "이미지 파일 정보 응답")
class ImageFileInfoResponse(
    @Schema(description = "아이디", example = "https://<bucket-name>.s3.amazonaws.com/<key>")
    @JsonProperty("uploadPath")
    val uploadPath: String
)

/**
 * CmsFileInfo Entity를 ImageFileInfoResponse로 변환한다.
 * @return ImageFileInfoResponse
 */
fun CmsFileInfo.toImageResponse(): ImageFileInfoResponse = ImageFileInfoResponse(
    uploadPath = id.getS3Url()
)
