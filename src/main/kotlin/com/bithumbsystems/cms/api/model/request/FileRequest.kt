package com.bithumbsystems.cms.api.model.request

import org.springframework.http.codec.multipart.FilePart
import org.springframework.http.codec.multipart.Part

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
}
