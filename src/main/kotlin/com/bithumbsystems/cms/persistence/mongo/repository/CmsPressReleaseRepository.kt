package com.bithumbsystems.cms.persistence.mongo.repository

import com.bithumbsystems.cms.persistence.mongo.entity.CmsPressRelease
import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Repository

@Repository
interface CmsPressReleaseRepository : CmsCommonRepository<CmsPressRelease> {

    fun findTop5ByIsDraftIsFalseOrderByScreenDateDescCreateDateDesc(): Flow<CmsPressRelease>
}
