package com.bithumbsystems.cms.api.util

import com.bithumbsystems.cms.api.config.aws.AwsProperties
import com.bithumbsystems.cms.api.exception.UploadFailedException
import com.bithumbsystems.cms.api.exception.ValidationException
import com.bithumbsystems.cms.api.model.constants.ConstraintConstants
import com.bithumbsystems.cms.api.model.constants.ConstraintConstants.S3_BUFFER_SIZE
import com.bithumbsystems.cms.api.model.enums.ErrorCode
import com.bithumbsystems.cms.api.model.enums.FileExtensionType
import com.bithumbsystems.cms.api.util.EnumUtil.findBy
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.apache.commons.io.FilenameUtils
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import software.amazon.awssdk.core.SdkResponse
import software.amazon.awssdk.core.async.AsyncRequestBody
import software.amazon.awssdk.core.async.AsyncResponseTransformer
import software.amazon.awssdk.core.async.ResponsePublisher
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.*
import java.net.URLEncoder
import java.nio.ByteBuffer
import java.util.concurrent.CompletableFuture

fun FilePart.upload(fileKey: String, s3AsyncClient: S3AsyncClient, bucket: String): String {
    saveFile(fileKey, s3AsyncClient, bucket, this).subscribe()
    return fileKey
}

suspend fun FilePart.validate(): Boolean {
    takeIf { (this.content().count().awaitSingleOrNull() ?: 0) > ConstraintConstants.MAX_FILE_SIZE }?.let {
        throw ValidationException(ErrorCode.INVALID_FILE_SIZE.message)
    }
    takeIf { FileExtensionType::name findBy (FilenameUtils.getExtension(this.filename())) == null }?.let {
        throw ValidationException(ErrorCode.INVALID_FILE_FORMAT.message)
    }
    return true
}

fun String.getFileName(): String = FilenameUtils.getBaseName(this)

fun String.getFileExtensionType(): FileExtensionType =
    (FileExtensionType::name findBy (FilenameUtils.getExtension(this))) ?: throw ValidationException(ErrorCode.INVALID_FILE_FORMAT.message)

fun String.getS3Url(): String =
    KotlinAwsProperties.awsProperties.let {
        "${it.s3Url}/$this"
    }

fun String.download(s3AsyncClient: S3AsyncClient, bucket: String): Mono<ResponsePublisher<GetObjectResponse>> {
    val objectRequest: GetObjectRequest = GetObjectRequest.builder()
        .bucket(bucket)
        .key(this)
        .build()

    val future: CompletableFuture<ResponsePublisher<GetObjectResponse>> = s3AsyncClient.getObject(
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

private class UploadState(var bucket: String, var fileKey: String) {
    var uploadId: String? = null
    var partCounter = 0
    var completedParts: MutableMap<Int, CompletedPart> = HashMap()
    var buffered = 0
}

fun saveFile(fileKey: String, s3AsyncClient: S3AsyncClient, bucket: String, part: FilePart): Mono<String> {
    val metadata: MutableMap<String, String> = HashMap()
    metadata["filename"] = URLEncoder.encode(part.filename(), Charsets.UTF_8)
    val uploadState = UploadState(bucket, fileKey)
    return Mono
        .fromFuture(
            uploadRequest(s3AsyncClient, part, fileKey, metadata, bucket)
        )
        .flatMapMany { response: CreateMultipartUploadResponse ->
            checkResult(response)
            uploadState.uploadId = response.uploadId()
            part.content()
        }
        .bufferUntil { buffer: DataBuffer ->
            uploadState.buffered += buffer.readableByteCount()
            if (uploadState.buffered >= S3_BUFFER_SIZE) {
                uploadState.buffered = 0
                return@bufferUntil true
            } else {
                return@bufferUntil false
            }
        }
        .map { buffers: List<DataBuffer> ->
            concatBuffers(buffers)
        }
        .flatMap { buffer: ByteBuffer ->
            uploadPart(s3AsyncClient, uploadState, buffer)
        }
        .reduce(
            uploadState
        ) { state: UploadState, completedPart: CompletedPart ->
            state.completedParts[completedPart.partNumber()] = completedPart
            state
        }
        .flatMap { state: UploadState ->
            completeUpload(s3AsyncClient, state)
        }
        .map { response: SdkResponse ->
            checkResult(response)
            uploadState.fileKey
        }
}

private fun uploadRequest(
    s3AsyncClient: S3AsyncClient,
    part: FilePart,
    fileKey: String,
    metadata: MutableMap<String, String>,
    bucket: String
): CompletableFuture<CreateMultipartUploadResponse> = s3AsyncClient
    .createMultipartUpload(
        CreateMultipartUploadRequest.builder()
            .contentType((part.headers().contentType ?: MediaType.APPLICATION_OCTET_STREAM).toString())
            .key(fileKey)
            .metadata(metadata)
            .bucket(bucket)
            .build()
    )

private fun concatBuffers(buffers: List<DataBuffer>): ByteBuffer {
    val partSize: Int = buffers.sumOf { it.readableByteCount() }
    val partData: ByteBuffer = ByteBuffer.allocate(partSize)
    buffers.forEach { buffer -> partData.put(buffer.asByteBuffer()) }

    // Reset read pointer to first byte
    partData.rewind()
    return partData
}

private fun uploadPart(s3AsyncClient: S3AsyncClient, uploadState: UploadState, buffer: ByteBuffer): Mono<CompletedPart> {
    val partNumber: Int = ++uploadState.partCounter
    val request: CompletableFuture<UploadPartResponse> = s3AsyncClient.uploadPart(
        UploadPartRequest.builder()
            .bucket(uploadState.bucket)
            .key(uploadState.fileKey)
            .partNumber(partNumber)
            .uploadId(uploadState.uploadId)
            .contentLength(buffer.capacity().toLong())
            .build(),
        AsyncRequestBody.fromPublisher(Mono.just(buffer))
    )
    return Mono
        .fromFuture(request)
        .map { uploadPartResult ->
            checkResult(uploadPartResult)
            CompletedPart.builder()
                .eTag(uploadPartResult.eTag())
                .partNumber(partNumber)
                .build()
        }
}

private fun completeUpload(s3AsyncClient: S3AsyncClient, state: UploadState): Mono<CompleteMultipartUploadResponse> {
    val multipartUpload: CompletedMultipartUpload = CompletedMultipartUpload.builder()
        .parts(state.completedParts.values)
        .build()
    return Mono.fromFuture(
        s3AsyncClient.completeMultipartUpload(
            CompleteMultipartUploadRequest.builder()
                .bucket(state.bucket)
                .uploadId(state.uploadId)
                .multipartUpload(multipartUpload)
                .key(state.fileKey)
                .build()
        )
    )
}

private fun checkResult(result: SdkResponse) {
    if (result.sdkHttpResponse() == null || !result.sdkHttpResponse().isSuccessful) {
        throw UploadFailedException(response = result)
    }
}
