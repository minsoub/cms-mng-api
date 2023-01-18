package com.bithumbsystems.cms.persistence.mongo.repository

import com.bithumbsystems.cms.persistence.mongo.entity.CmsNotice
import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
interface CmsNoticeRepository : CmsCommonRepository<CmsNotice> {
    fun findByIsShowIsTrueAndIsDeleteIsFalseAndIsDraftIsFalseAndIsScheduleIsFalseOrderByScreenDateDesc(pageable: Pageable): Flow<CmsNotice>

    fun findByIsBannerIsTrueAndIsDraftIsFalseAndIsScheduleIsFalse(): Flow<CmsNotice>
}
