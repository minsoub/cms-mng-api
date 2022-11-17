package com.bithumbsystems.cms.api.service

import com.bithumbsystems.cms.api.config.aws.AwsProperties
import com.bithumbsystems.cms.api.config.operator.ServiceOperator.executeIn
import com.bithumbsystems.cms.api.model.aggregate.FileResult
import com.bithumbsystems.cms.api.util.Logger
import com.bithumbsystems.cms.api.util.download
import com.bithumbsystems.cms.api.util.upload
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import software.amazon.awssdk.services.s3.S3AsyncClient

@Service
class FileService(
    private val ioDispatcher: CoroutineDispatcher,
    private val awsProperties: AwsProperties,
    private val s3AsyncClient: S3AsyncClient
) {

    private val logger by Logger()

    suspend fun upload(file: FilePart) = executeIn(
        dispatcher = ioDispatcher,
        action = { file.upload(s3AsyncClient, awsProperties.bucket) }
    )

    fun download(fileKey: String): Mono<FileResult> = mono {
        logger.info("download fileKey: $fileKey")
        FileResult(
            fileName = "",
            result = fileKey.download(s3AsyncClient = s3AsyncClient, bucket = awsProperties.bucket).awaitSingle()
        )
    }
}
