import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.6.7"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
    id("io.gitlab.arturbosch.detekt") version "1.21.0"
    id("org.jetbrains.kotlinx.kover") version "0.6.1"
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
    kotlin("kapt") version "1.6.21"
}

group = "com.bithumbsystems"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17
val springVersion = "2.6.7"
val openapiVersion = "1.6.9"
val awssdkVersion = "2.19.8"
repositories {
    mavenCentral()
}

dependencies {
    // Spring
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-configuration-processor:$springVersion")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

    // Kotlin
    kapt("org.springframework.boot:spring-boot-configuration-processor:$springVersion")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("com.michael-bull.kotlin-result:kotlin-result:1.1.16")
    implementation("com.michael-bull.kotlin-result:kotlin-result-coroutines:1.1.16")

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.21.0")

    // open api
    implementation("org.springdoc:springdoc-openapi-webflux-ui:$openapiVersion")
    implementation("org.springdoc:springdoc-openapi-kotlin:$openapiVersion")

    // netty M1
    runtimeOnly("io.netty:netty-resolver-dns-native-macos:4.1.77.Final:osx-aarch_64")

    // redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    implementation("org.redisson:redisson-spring-boot-starter:3.17.6")
    implementation("it.ozimov:embedded-redis:0.7.1")

    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")

    implementation("com.google.code.gson:gson:2.9.0")

    implementation("software.amazon.awssdk:bom:$awssdkVersion")
    implementation("software.amazon.awssdk:aws-core:$awssdkVersion")
    implementation("software.amazon.awssdk:auth")
    implementation("software.amazon.awssdk:kms:$awssdkVersion")
    implementation("software.amazon.awssdk:ssm:$awssdkVersion")
    implementation("software.amazon.awssdk:s3:$awssdkVersion")
    implementation("software.amazon.awssdk:netty-nio-client:$awssdkVersion")

    implementation("io.awspring.cloud:spring-cloud-starter-aws:2.4.1")
    implementation("io.awspring.cloud:spring-cloud-starter-aws-messaging:2.4.1")
    implementation("io.awspring.cloud:spring-cloud-starter-aws-parameter-store-config:2.4.1")

    // excel
    implementation("org.apache.poi:poi:5.2.3")
    implementation("org.apache.poi:poi-ooxml:5.2.3")

    // test
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("org.amshove.kluent:kluent:1.68")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    testImplementation("io.projectreactor:reactor-test:3.4.18")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.2")
    testImplementation("io.mockk:mockk:1.12.4")
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config = files("$rootDir/config/detekt.yml")
}

tasks {
    getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
        enabled = true
        mainClass.set("com.bithumbsystems.cms.CmsMngApiApplicationKt")
    }

    jar {
        enabled = false
    }

    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "17"
        }
    }

    withType<Test> {
        useJUnitPlatform()
    }

    getByName<Test>("test") {
        systemProperty("spring.profiles.active", "test")
    }
}

kover {
    verify {
        onCheck.set(true)
        rule {
            isEnabled = true
            name = null
            target = kotlinx.kover.api.VerificationTarget.ALL

            bound {
                minValue = 60
                maxValue = 100
                counter = kotlinx.kover.api.CounterType.LINE
                valueType = kotlinx.kover.api.VerificationValueType.COVERED_PERCENTAGE
            }
        }
    }
}
