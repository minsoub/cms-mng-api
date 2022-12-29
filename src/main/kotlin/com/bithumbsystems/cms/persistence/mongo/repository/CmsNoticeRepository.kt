package com.bithumbsystems.cms.persistence.mongo.repository

import com.bithumbsystems.cms.persistence.mongo.entity.CmsNotice
import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Repository

@Repository
interface CmsNoticeRepository : CmsCommonRepository<CmsNotice> {
    fun findTop5ByIsDraftIsFalseOrderByScreenDateDescCreateDateDesc(): Flow<CmsNotice>

    fun findAllByIsBannerIsTrueAndIsDraftIsFalseAndIsScheduleIsFalse(): Flow<CmsNotice>
}
