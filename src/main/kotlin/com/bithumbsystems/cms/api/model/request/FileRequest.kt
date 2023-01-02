package com.bithumbsystems.cms.api.model.request

import com.bithumbsystems.cms.api.model.constants.ConstraintConstants.MAX_FILE_SIZE
import kotlinx.coroutines.reactor.awaitSingleOrNull
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
    takeIf { it.file != null }?.let {
        fileKey = UUID.randomUUID().toString().replace("-", "")
    }

    takeIf { it.shareFile != null }?.let {
        shareFileKey = UUID.randomUUID().toString().replace("-", "")
    }

    takeIf { it.thumbnailFile != null }?.let {
        thumbnailFileKey = UUID.randomUUID().toString().replace("-", "")
    }
}

suspend fun FileRequest?.validate(): Boolean {
    return when {
        (this?.file?.content()?.count()?.awaitSingleOrNull() ?: 0) > MAX_FILE_SIZE -> false
        (this?.shareFile?.content()?.count()?.awaitSingleOrNull() ?: 0) > MAX_FILE_SIZE -> false
        (this?.thumbnailFile?.content()?.count()?.awaitSingleOrNull() ?: 0) > MAX_FILE_SIZE -> false
        else -> true
    }
}
