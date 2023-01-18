package com.bithumbsystems.cms.persistence.mongo.repository

import com.bithumbsystems.cms.persistence.mongo.entity.CmsNoticeCategory
import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.repository.kotlin.CoroutineSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface CmsNoticeCategoryRepository : CoroutineSortingRepository<CmsNoticeCategory, String> {

    fun findByCriteria(criteria: Criteria, pageable: Pageable, sort: Sort): Flow<CmsNoticeCategory>
}
