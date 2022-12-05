package com.bithumbsystems.cms.api.util

import com.bithumbsystems.cms.api.util.NullUtil.letIfAllNotNull
import org.amshove.kluent.`should be null`
import org.junit.jupiter.api.Test

class NullUtilTest {

    @Test
    fun `let 여러개 처리 유틸 테스트`() {
        val test = null
        letIfAllNotNull(test, test) {
            it.`should be null`()
        }
    }
}
