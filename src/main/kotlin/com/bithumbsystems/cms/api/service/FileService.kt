package com.bithumbsystems.cms.api.service

import com.bithumbsystems.cms.api.config.aws.AwsProperties
import com.bithumbsystems.cms.api.config.operator.ServiceOperator.executeIn
import com.bithumbsystems.cms.api.config.resolver.Account
import com.bithumbsystems.cms.api.model.aggregate.FileResult
import com.bithumbsystems.cms.api.model.request.*
import com.bithumbsystems.cms.api.model.response.ErrorData
import com.bithumbsystems.cms.api.model.response.FileInfoResponse
import com.bithumbsystems.cms.api.model.response.toResponse
import com.bithumbsystems.cms.api.util.*
import com.bithumbsystems.cms.persistence.mongo.repository.CmsFileInfoRepository
import com.github.michaelbull.result.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
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

    suspend fun upload(fileKey: String, file: FilePart) = executeIn(
        dispatcher = ioDispatcher,
        action = { file.upload(fileKey, s3AsyncClient, awsProperties.bucket) }
    )

    suspend fun addFileInfo(file: FilePart, account: Account, fileSize: Long?): Result<FileInfoResponse?, ErrorData> = executeIn(
        dispatcher = ioDispatcher,
        action = {
            upload(file).component1()?.let {
                val fileInfoRequest = FileInfoRequest(
                    id = it,
                    name = file.filename().getFileName(),
                    size = fileSize ?: file.content().count().awaitSingle(),
                    extension = file.filename().getFileExtensionType()
                )
                fileInfoRequest.setCreateInfo(account)
                fileInfoRepository.save(fileInfoRequest.toEntity()).toResponse()
            }
        }
    )

    suspend fun addFileInfo(fileKey: String, file: FilePart, account: Account, fileSize: Long?): Result<FileInfoResponse?, ErrorData> = executeIn(
        dispatcher = ioDispatcher,
        action = {
            coroutineScope {
                launch {
                    upload(fileKey, file)
                }

                FileInfoRequest(
                    id = fileKey,
                    name = file.filename().getFileName(),
                    size = fileSize ?: file.content().count().awaitSingle(),
                    extension = file.filename().getFileExtensionType()
                ).run {
                    this.setCreateInfo(account)
                    fileInfoRepository.save(this.toEntity()).toResponse()
                }
            }
        }
    )

    suspend fun addFileInfo(
        fileRequest: FileRequest?,
        account: Account,
        request: CommonBoardRequest
    ) = executeIn(
        dispatcher = ioDispatcher,
        action = {
            coroutineScope {
                fileRequest?.let {
                    launch {
                        it.file?.let { file ->
                            it.fileKey?.let { fileKey ->
                                addFileInfo(fileKey = fileKey, file = file, account = account, fileSize = null)
                                request.fileId = fileKey
                            }
                        }
                    }

                    launch {
                        it.shareFile?.let { shareFile ->
                            it.shareFileKey?.let { shareFileKey ->
                                addFileInfo(fileKey = shareFileKey, file = shareFile, account = account, fileSize = null)
                                request.shareFileId = shareFileKey
                            }
                        }
                    }
                }
            }
        }
    )

    suspend fun getFileInfo(id: String): Result<FileInfoResponse?, ErrorData> = executeIn {
        fileInfoRepository.findById(id)?.toResponse()
    }

    fun download(fileKey: String): Mono<FileResult> = mono {
        logger.info("download fileKey: $fileKey")
        val fileInfo: FileInfoResponse? = fileInfoRepository.findById(fileKey)?.toResponse()
        FileResult(
            fileName = fileInfo?.name.plus(".".plus(fileInfo?.extension?.name?.lowercase())),
            result = fileKey.download(s3AsyncClient = s3AsyncClient, bucket = awsProperties.bucket).awaitSingle()
        )
    }
}
