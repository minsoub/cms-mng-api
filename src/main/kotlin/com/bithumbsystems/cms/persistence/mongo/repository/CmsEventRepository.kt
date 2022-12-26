package com.bithumbsystems.cms.persistence.mongo.repository

import com.bithumbsystems.cms.persistence.mongo.entity.CmsEvent
import org.springframework.stereotype.Repository

@Repository
interface CmsEventRepository : CmsCommonRepository<CmsEvent>
