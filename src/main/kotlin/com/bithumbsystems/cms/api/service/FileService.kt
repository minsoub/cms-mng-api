package com.bithumbsystems.cms.api.service

import com.bithumbsystems.cms.api.config.aws.AwsProperties
import com.bithumbsystems.cms.api.config.operator.ServiceOperator.executeIn
import com.bithumbsystems.cms.api.config.resolver.Account
import com.bithumbsystems.cms.api.model.aggregate.FileResult
import com.bithumbsystems.cms.api.model.request.FileInfoRequest
import com.bithumbsystems.cms.api.model.request.FileRequest
import com.bithumbsystems.cms.api.model.request.setCreateInfo
import com.bithumbsystems.cms.api.model.request.toEntity
import com.bithumbsystems.cms.api.model.response.*
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
import java.net.URLEncoder
import java.util.*

@Service
class FileService(
    private val ioDispatcher: CoroutineDispatcher,
    private val awsProperties: AwsProperties,
    private val s3AsyncClient: S3AsyncClient,
    private val fileInfoRepository: CmsFileInfoRepository,
) : CmsBaseService() {

    suspend fun upload(fileKey: String, file: FilePart): Result<String?, ErrorData> =
        executeIn(dispatcher = ioDispatcher, action = { file.upload(fileKey, s3AsyncClient, awsProperties.bucket) })

    suspend fun addFileInfo(file: FilePart, account: Account): Result<FileInfoResponse?, ErrorData> =
        addFileInfo(fileKey = UUID.randomUUID().toString().replace("-", ""), file = file, account = account)

    private suspend fun fileInfoRequest(
        id: String,
        file: FilePart,
        account: Account
    ): FileInfoRequest {
        val fileInfoRequest = FileInfoRequest(
            id = id,
            name = file.filename().getFileName(),
            size = file.content().count().awaitSingle(),
            extension = file.filename().getFileExtensionType()
        )
        fileInfoRequest.setCreateInfo(
            password = awsProperties.kmsKey,
            saltKey = awsProperties.saltKey,
            ivKey = awsProperties.ivKey,
            account = account
        )
        return fileInfoRequest
    }

    suspend fun addImageFileInfo(file: FilePart, account: Account): ImageFileInfoResponse? = coroutineScope {
        val fileKey: String = UUID.randomUUID().toString().replace("-", "")
        launch {
            upload(fileKey, file)
        }

        fileInfoRepository.save(fileInfoRequest(id = fileKey, file = file, account = account).toEntity()).toImageResponse()
    }

    suspend fun addFileInfo(fileKey: String, file: FilePart, account: Account): Result<FileInfoResponse?, ErrorData> =
        executeIn(dispatcher = ioDispatcher, validator = { file.validate() }, action = {
            coroutineScope {
                launch {
                    upload(fileKey, file)
                }

                fileInfoRepository.save(fileInfoRequest(id = fileKey, file = file, account = account).toEntity()).toResponse(awsProperties.kmsKey)
            }
        })

    suspend fun addFileInfo(
        fileRequest: FileRequest?,
        account: Account
    ) = executeIn(dispatcher = ioDispatcher, action = {
        coroutineScope {
            fileRequest?.let {
                launch {
                    it.file?.let { file ->
                        it.fileKey?.let { fileKey ->
                            addFileInfo(fileKey = fileKey, file = file, account = account)
                        }
                    }
                }

                launch {
                    it.shareFile?.let { shareFile ->
                        it.shareFileKey?.let { shareFileKey ->
                            addFileInfo(fileKey = shareFileKey, file = shareFile, account = account)
                        }
                    }
                }

                launch {
                    it.thumbnailFile?.let { thumbnailFile ->
                        it.thumbnailFileKey?.let { thumbnailFileKey ->
                            addFileInfo(fileKey = thumbnailFileKey, file = thumbnailFile, account = account)
                        }
                    }
                }
            }
        }
    })

    suspend fun getFileInfo(id: String): Result<FileInfoResponse?, ErrorData> = executeIn {
        fileInfoRepository.findById(id)?.toResponse(awsProperties.kmsKey)
    }

    fun download(fileKey: String): Mono<FileResult> = mono {
        logger.info("download fileKey: $fileKey")
        val fileInfo: FileInfoResponse? = fileInfoRepository.findById(fileKey)?.toResponse(awsProperties.kmsKey)
        FileResult(
            fileName = URLEncoder.encode(fileInfo?.name.plus(".".plus(fileInfo?.extension?.name?.lowercase())), Charsets.UTF_8),
            result = fileKey.download(s3AsyncClient = s3AsyncClient, bucket = awsProperties.bucket).awaitSingle()
        )
    }
}
