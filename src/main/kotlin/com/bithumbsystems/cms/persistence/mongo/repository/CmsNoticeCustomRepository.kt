package com.bithumbsystems.cms.persistence.mongo.repository

import com.bithumbsystems.cms.persistence.mongo.entity.CmsNotice
import kotlinx.coroutines.flow.Flow

interface CmsNoticeCustomRepository {

    fun findTop5ByIsDraftIsFalseOrderByScreenDateDescCreateDateDesc(): Flow<CmsNotice>
}
