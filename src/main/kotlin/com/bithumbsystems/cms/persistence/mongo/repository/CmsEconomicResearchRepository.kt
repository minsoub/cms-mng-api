package com.bithumbsystems.cms.persistence.mongo.repository

import com.bithumbsystems.cms.persistence.mongo.entity.CmsEconomicResearch
import org.springframework.data.repository.kotlin.CoroutineSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface CmsEconomicResearchRepository : CoroutineSortingRepository<CmsEconomicResearch, String>
