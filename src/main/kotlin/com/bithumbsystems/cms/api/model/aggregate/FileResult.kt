package com.bithumbsystems.cms.api.model.aggregate

import software.amazon.awssdk.core.async.ResponsePublisher
import software.amazon.awssdk.services.s3.model.GetObjectResponse

class FileResult(
    val result: ResponsePublisher<GetObjectResponse>,
    val fileName: String
)
