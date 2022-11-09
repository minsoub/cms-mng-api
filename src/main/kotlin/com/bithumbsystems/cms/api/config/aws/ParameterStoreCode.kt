package com.bithumbsystems.cms.api.config.aws

enum class ParameterStoreCode(val value: String) {
    DB_URL("dburl"),
    DB_USER("user"),
    DB_PORT("port"),
    DB_NAME("name"),
    DB_PASSWORD("passwd"),
    KMS_ALIAS_NAME("key"),
    JWT_SECRET_KEY("jwt_secret_key"),
    CRYPTO_KEY("key")
}
