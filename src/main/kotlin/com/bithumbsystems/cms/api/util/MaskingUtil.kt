package com.bithumbsystems.cms.api.util

import org.springframework.util.StringUtils

object MaskingUtil {
    /**
     * 이메일 마스킹
     * 이메일 주소는 아이디의 앞 3자리 및 @ 이후를 제외한 문자, 숫자 표시 제한(abc****@abc.com, abc@abc.com)
     * @return 마스킹 처리된 이메일
     */
    fun String.toEmailMask(): String =
        if (StringUtils.hasLength(this)) {
            this.replace(Regex("(?<=.{3}).(?=.*@)"), "*")
        } else {
            this
        }

    /**
     * 이름 마스킹
     * 성을 제외한 이름의 첫 번째 자리 표시 제한(ex : 홍*동, 남궁*분, 고*)
     * @return 마스킹 처리된 이름
     */
    fun String.toNameMask(): String =
        if (StringUtils.hasLength(this)) {
            // 이름이 외자 또는 4자 이상인 경우 분기
            val middleMask: String = if (this.length > 2) {
                this.substring(1, this.length - 1)
            } else {
                this.substring(1, this.length) // 외자
            }
            // 마스킹 변수 선언(*)
            var masking = ""
            // 가운데 글자 마스킹 하기위한 증감값
            middleMask.indices.forEach { _ ->
                masking += "*"
            }
            // 이름이 외자 또는 4자 이상인 경우 분기
            val result = if (this.length > 2) {
                "${this.substring(0, 1)}${middleMask.replace(middleMask, masking)}${this.substring(this.length - 1, this.length)}"
            } else { // 외자인경우
                "${this.substring(0, 1)}${middleMask.replace(middleMask, masking)}"
            }
            result
        } else {
            this
        }
}
