package com.bithumbsystems.cms.api.controller

import com.bithumbsystems.cms.api.util.Logger
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class CommonControllerTest internal constructor() {
    val logger by Logger()

    companion object {
        internal const val token =
            "eyJhbGciOiJIUzUxMiJ9.eyJhY2NvdW50X2lkIjoiYmRhMGY4ZjAzZjhlMTFlZGI4NzgwMjQyYWMxMjAwMDIiLCJST0xFIjpbIkNQQy1TSVRFLUFETUlOIiwiTFJDLVNJVEUtQ" +
                "URNSU4iLCJTVVBFUi1BRE1JTiJdLCJ1c2VyX2lkIjoiam1jQGJpdGh1bWJzeXN0ZW1zLmNvbSIsImlzcyI6ImptY0BiaXRodW1ic3lzdGVtcy5jb20iLCJzdWIiOiI2MmE" +
                "xNWY0YWU0MTI5YjUxOGIxMzMxMjgiLCJpYXQiOjE2Njk5NzQwMTIsImp0aSI6ImYwZDY3ZWQ0LTRkZGMtNDJlMi05NTg0LWI1OWE1ZGViYzZiMyIsImV4cCI6OTI0NjQ0N" +
                "zAwMDB9.7M9HsUH9K4qg-c4RKrohujmR83InWr1UJr2kZkcLKD_Sp1I4IoByaCF-yaBB9i3i8KAQryVTlC9OxkA4TCnyEg"
        internal lateinit var id: String
    }
}
