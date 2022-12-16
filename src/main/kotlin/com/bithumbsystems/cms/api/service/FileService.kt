package com.bithumbsystems.cms.api.service

import com.bithumbsystems.cms.api.config.aws.AwsProperties
import com.bithumbsystems.cms.api.config.operator.ServiceOperator.executeIn
import com.bithumbsystems.cms.api.config.resolver.Account
import com.bithumbsystems.cms.api.model.aggregate.FileResult
import com.bithumbsystems.cms.api.model.request.*
import com.bithumbsystems.cms.api.model.response.ErrorData
import com.bithumbsystems.cms.api.model.response.toResponse
import com.bithumbsystems.cms.api.util.*
import com.bithumbsystems.cms.persistence.mongo.repository.CmsFileInfoRepository
import com.github.michaelbull.result.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
    private val s3AsyncClient: S3AsyncClient,
    private val fileInfoRepository: CmsFileInfoRepository
) {

    private val logger by Logger()

    suspend fun upload(file: FilePart) = executeIn(
        dispatcher = ioDispatcher,
        action = { file.upload(s3AsyncClient, awsProperties.bucket) }
    )

    suspend fun addFileInfo(file: FilePart, account: Account) = executeIn {
        coroutineScope {
            val id: Deferred<Result<String?, ErrorData>> = async {
                upload(file)
            }

            id.await().component1()?.let {
                val fileInfoRequest = FileInfoRequest(
                    id = it,
                    name = file.filename().getFileName(),
                    size = file.headers().contentLength, // todo 파일 사이즈 로직 변경
                    extension = file.filename().getFileExtensionType()
                )
                fileInfoRequest.setCreateInfo(account)
                fileInfoRepository.save(fileInfoRequest.toEntity()).toResponse()
            }
        }
    }

    suspend fun addFileInfo(
        fileRequest: FileRequest?,
        account: Account,
        request: CommonBoardRequest
    ) {
        fileRequest?.let {
            it.file?.let { file ->
                addFileInfo(file, account).component1()?.let { fileResponse ->
                    request.fileId = fileResponse.id
                }
            }

            it.shareFile?.let { shareFile ->
                addFileInfo(shareFile, account).component1()?.let { fileResponse ->
                    request.shareFileId = fileResponse.id
                }
            }
        }
    }

    fun download(fileKey: String): Mono<FileResult> = mono {
        logger.info("download fileKey: $fileKey")
        FileResult(
            fileName = "",
            result = fileKey.download(s3AsyncClient = s3AsyncClient, bucket = awsProperties.bucket).awaitSingle()
        )
    }
}
