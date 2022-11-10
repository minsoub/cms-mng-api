# 콘텐츠 통합 관리 API
- Backoffice

## 사용 기술

- JDK 17
- Kotlin
    - Kotlin Coroutine
- Spring Boot
    - WebFlux
    - Docs Openapi
- Test
    - junit5
    - mockk
    - kluent
    - kover
        - test coverage
- Datasource
    - MongoDB
    - Redis
        - redisson

##  프로젝트 구성

### 코딩 스타일

- Detekt(정적분석)
- Ktlint(스타일)
- kotlin-result(https://github.com/michaelbull/kotlin-result)

### 유의사항
M1의 경우 build.gradle.kts dependency 에 추가
(현재 추가 상태)
```
runtimeOnly("io.netty:netty-resolver-dns-native-macos:4.1.77.Final:osx-aarch_64")
```

IDE Ktlint 설정
![img.png](img.png)
```
./gradlew addKtlintCheckGitPreCommitHook
```

detekt
```
./gradlew detekt
```

kover
```
./gradlew koverVerify
```


### Swagger API 테스트 경로

```
http://localhost:8080/swagger-ui.html
```

### 코루틴 확인
JVM 옵션을 사용
```
-Dkotlinx.coroutines.debug
```


### 환경 변수 설정
JVM 옵션을 사용(local, dev, qa, prod, eks-dev)
```
-Dspring.profiles.active=local
```
