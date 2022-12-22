package com.bithumbsystems.cms.persistence.mongo.repository

import com.bithumbsystems.cms.persistence.mongo.entity.CmsNotice
import org.springframework.stereotype.Repository

@Repository
interface CmsNoticeRepository : CmsCommonCustomRepository<CmsNotice>
