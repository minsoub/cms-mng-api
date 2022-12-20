package com.bithumbsystems.cms.api.service

import com.bithumbsystems.cms.api.config.operator.ServiceOperator.executeIn
import com.bithumbsystems.cms.api.config.resolver.Account
import com.bithumbsystems.cms.api.model.request.*
import com.bithumbsystems.cms.api.model.response.*
import com.bithumbsystems.cms.api.util.QueryUtil.buildCriteria
import com.bithumbsystems.cms.api.util.QueryUtil.buildSort
import com.bithumbsystems.cms.persistence.mongo.entity.CmsPressRelease
import com.bithumbsystems.cms.persistence.mongo.repository.CmsCustomRepository
import com.bithumbsystems.cms.persistence.mongo.repository.CmsPressReleaseRepository
import com.github.michaelbull.result.Result
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Service

@Service
class PressReleaseService(
    private val pressReleaseRepository: CmsPressReleaseRepository,
    private val pressReleaseCustomRepository: CmsCustomRepository<CmsPressRelease>,
    private val fileService: FileService
) {

    /**
     * 보도자료 생성
     * @param request 보도자료 등록 요청
     * @param fileRequest 첨부 파일 및 공유 태그 파일
     */
    suspend fun createPressRelease(
        request: PressReleaseRequest,
        fileRequest: FileRequest?,
        account: Account
    ): Result<PressReleaseDetailResponse?, ErrorData> = executeIn(
        validator = {
            request.validate()
        },
        action = {
            fileService.addFileInfo(fileRequest = fileRequest, account = account, request = request)

            pressReleaseRepository.save(request.toEntity()).toResponse() // todo
        }
    )

    suspend fun getPressReleases(searchParams: SearchParams): Result<ListResponse<PressReleaseResponse>?, ErrorData> = executeIn {
        coroutineScope {
            val criteria: Criteria = searchParams.buildCriteria(isFixTop = null, isDelete = false)
            val sort: Sort = searchParams.buildSort()

            val notices: Deferred<List<PressReleaseResponse>> = async {
                pressReleaseCustomRepository.findAllByCriteria(
                    criteria = criteria,
                    pageable = Pageable.unpaged(),
                    sort = sort
                )
                    .map { it.toMaskingResponse() }
                    .toList()
            }

            // todo 로직들 반영

            ListResponse(
                contents = notices.await(),
                totalCounts = notices.await().size.toLong()
            )
        }
    }

    suspend fun getPressRelease(id: String) = executeIn {
        pressReleaseRepository.findById(id)?.toResponse()
    }

    suspend fun updatePressRelease(
        id: String,
        request: PressReleaseRequest,
        fileRequest: FileRequest?,
        account: Account
    ) = executeIn(
        validator = {
            request.validate()
        },
        action = {
            fileService.addFileInfo(fileRequest = fileRequest, account = account, request = request)

            println("$id, $account")

            pressReleaseRepository.save(request.toEntity()).toResponse() // todo
        }
    )

    suspend fun deletePressRelease(id: String, account: Account) = executeIn {
        val pressRelease: CmsPressRelease? = getPressRelease(id).component1()?.toEntity()

        println(account)

        pressRelease?.let {
            pressReleaseRepository.save(pressRelease).toResponse() // todo
        }
    }
}
