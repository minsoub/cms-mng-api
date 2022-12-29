package com.bithumbsystems.cms.api.util

import com.bithumbsystems.cms.api.config.aws.AwsProperties
import com.bithumbsystems.cms.api.model.enums.FileExtensionType
import com.bithumbsystems.cms.api.util.EnumUtil.findBy
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitSingle
import org.apache.commons.io.FilenameUtils
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import software.amazon.awssdk.core.async.AsyncRequestBody
import software.amazon.awssdk.core.async.AsyncResponseTransformer
import software.amazon.awssdk.core.async.ResponsePublisher
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectResponse
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.ByteArrayOutputStream
import java.util.*

suspend fun FilePart.upload(s3AsyncClient: S3AsyncClient, bucket: String): String {
    val fileKey: String = UUID.randomUUID().toString().replace("-", "")
    val objectRequest: PutObjectRequest = PutObjectRequest.builder()
        .bucket(bucket)
        .key(fileKey)
        .contentLength(content().count().awaitSingle() * 1024)
        .build()

    s3AsyncClient.putObject(
        objectRequest,
        AsyncRequestBody.fromBytes(this.toBytes())
    ).join()
    return fileKey
}

suspend fun FilePart.upload(fileKey: String, s3AsyncClient: S3AsyncClient, bucket: String): String {
    val objectRequest: PutObjectRequest = PutObjectRequest.builder()
        .bucket(bucket)
        .key(fileKey)
        .build()

    s3AsyncClient.putObject(
        objectRequest,
        AsyncRequestBody.fromBytes(this.toBytes())
    ).join()
    return fileKey
}

suspend fun FilePart.toBytes(): ByteArray {
    val bytesList: List<ByteArray> = this.content()
        .flatMap { dataBuffer -> Flux.just(dataBuffer.asByteBuffer().array()) }
        .collectList()
        .awaitFirst()

    // concat ByteArrays
    val byteStream = ByteArrayOutputStream()
    bytesList.forEach { byteStream.write(it) }
    return byteStream.toByteArray()
}

fun String.getFileName(): String = FilenameUtils.getBaseName(this)

fun String.getFileExtensionType(): FileExtensionType =
    (FileExtensionType::name findBy (FilenameUtils.getExtension(this))) ?: throw IllegalArgumentException("파일 확장자가 올바르지 않습니다.")

fun String.getS3Url(): String =
    KotlinAwsProperties.awsProperties.let {
        "https://${it.bucket}.s3.${it.region}.amazonaws.com/$this"
    }

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

@Component
private class KotlinAwsProperties(
    awsProperties: AwsProperties
) {
    init {
        KotlinAwsProperties.awsProperties = awsProperties
    }

    companion object {
        lateinit var awsProperties: AwsProperties
    }
}
