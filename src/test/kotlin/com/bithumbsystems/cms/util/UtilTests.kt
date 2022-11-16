package com.bithumbsystems.cms.util

import com.bithumbsystems.cms.persistence.mongo.entity.Program
import com.bithumbsystems.cms.persistence.mongo.enums.ActionMethod
import com.bithumbsystems.cms.persistence.mongo.enums.RoleType
import com.google.gson.Gson
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

class UtilTests {

    @Test
    fun `프로그램 json 변환 테스트`() {
        val program = Program(
            name = "파일 업로드",
            type = RoleType.ADMIN,
            kindName = "게시글 > 등록/수정",
            actionMethod = ActionMethod.GET,
            actionUrl = "/api/v1/mng/files",
            isUse = true,
            description = "파일을 업로드 합니다.",
            siteId = "62a15f4ae4129b518b133129"
        )
        val json = Gson().toJson(program)
        val result = Gson().fromJson(json, Program::class.java)
        println(json)
        println(result)

        program.type `should be equal to` result.type
        program.kindName `should be equal to` result.kindName
        program.actionMethod `should be equal to` result.actionMethod
    }
}
