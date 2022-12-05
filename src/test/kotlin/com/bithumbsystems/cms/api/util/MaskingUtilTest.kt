package com.bithumbsystems.cms.api.util

import com.bithumbsystems.cms.api.util.MaskingUtil.getEmailMask
import com.bithumbsystems.cms.api.util.MaskingUtil.getNameMask
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

internal class MaskingUtilTest {
    @Test
    fun `이메일 3자리 초과 마스킹 테스트`() {
        val email = "abcde@example.com"
        val result = email.getEmailMask()

        println(email)
        println(result)

        result `should be equal to` "abc**@example.com"
    }

    @Test
    fun `이메일 3자리 이하 마스킹 테스트`() {
        val email = "abc@example.com"
        val result = email.getEmailMask()

        println(email)
        println(result)

        result `should be equal to` "abc@example.com"
    }

    @Test
    fun `길이 없는 마스킹 테스트`() {
        val email = ""
        val result = email.getEmailMask()

        println(email)
        println(result)

        result `should be equal to` ""
    }

    @Test
    fun `세글자 이름 마스킹 테스트`() {
        val name = "김이박"
        val result = name.getNameMask()

        println(name)
        println(result)

        result `should be equal to` "김*박"
    }

    @Test
    fun `외자 이름 마스킹 테스트`() {
        val name = "김이"
        val result = name.getNameMask()

        println(name)
        println(result)

        result `should be equal to` "김*"
    }

    @Test
    fun `길이 없는 이름 마스킹 테스트`() {
        val name = ""
        val result = name.getNameMask()

        println(name)
        println(result)

        result `should be equal to` ""
    }
}
