package com.bithumbsystems.cms.api.model.request

import com.bithumbsystems.cms.api.util.validate
import org.springframework.http.codec.multipart.FilePart
import org.springframework.http.codec.multipart.Part
import java.util.*

class FileRequest(
    val file: FilePart?,
    val shareFile: FilePart?,
    val thumbnailFile: FilePart?
) {
    constructor(file: FilePart?, shareFile: FilePart?) : this(file = file, shareFile = shareFile, thumbnailFile = null)
    constructor(file: Part?, shareFile: Part?, thumbnailFile: Part?) : this(
        file = file as? FilePart,
        shareFile = shareFile as? FilePart,
        thumbnailFile = thumbnailFile as? FilePart
    )

    var fileKey: String? = null
    var shareFileKey: String? = null
    var thumbnailFileKey: String? = null
}

fun FileRequest.setKeys() {
    file?.let {
        fileKey = UUID.randomUUID().toString().replace("-", "")
    }

    shareFile?.let {
        shareFileKey = UUID.randomUUID().toString().replace("-", "")
    }

    thumbnailFile?.let {
        thumbnailFileKey = UUID.randomUUID().toString().replace("-", "")
    }
}

suspend fun FileRequest?.validate(): Boolean {
    this?.file?.validate()
    this?.shareFile?.validate()
    this?.thumbnailFile?.validate()
    return true
}
