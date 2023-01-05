package com.bithumbsystems.cms.persistence.mongo.repository

import com.bithumbsystems.cms.persistence.mongo.entity.CmsPressRelease
import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
interface CmsPressReleaseRepository : CmsCommonRepository<CmsPressRelease> {

    fun findByIsShowIsTrueAndIsDeleteIsFalseAndIsDraftIsFalseOrderByScreenDateDesc(pageable: Pageable): Flow<CmsPressRelease>
}
