package com.bithumbsystems.cms.api.util

import com.bithumbsystems.cms.api.model.enums.FileExtensionType
import com.bithumbsystems.cms.api.util.EnumUtil.findBy
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.reactive.asFlow
import org.apache.commons.io.FilenameUtils
import org.springframework.http.codec.multipart.FilePart
import reactor.core.publisher.Mono
import software.amazon.awssdk.core.async.AsyncRequestBody
import software.amazon.awssdk.core.async.AsyncResponseTransformer
import software.amazon.awssdk.core.async.ResponsePublisher
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectResponse
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.util.*

suspend fun FilePart.upload(s3AsyncClient: S3AsyncClient, bucket: String): String {
    val fileKey = UUID.randomUUID().toString().replace("-", "")
    val objectRequest = PutObjectRequest.builder()
        .bucket(bucket)
        .key(fileKey)
        .build()

    this.content().asFlow().collect {
        s3AsyncClient.putObject(
            objectRequest,
            AsyncRequestBody.fromByteBuffer(it.asByteBuffer())
        )
    }
    return fileKey
}

fun String.getFileName(): String = FilenameUtils.getBaseName(this)

fun String.getFileExtensionType(): FileExtensionType =
    (FileExtensionType::name findBy (FilenameUtils.getExtension(this))) ?: throw IllegalArgumentException("파일 확장자가 올바르지 않습니다.")

fun String.download(s3AsyncClient: S3AsyncClient, bucket: String): Mono<ResponsePublisher<GetObjectResponse>> {
    val objectRequest: GetObjectRequest = GetObjectRequest.builder()
        .bucket(bucket)
        .key(this)
        .build()

    val future = s3AsyncClient.getObject(
        objectRequest,
        AsyncResponseTransformer.toPublisher()
    )

    return Mono.fromFuture(future)
}
