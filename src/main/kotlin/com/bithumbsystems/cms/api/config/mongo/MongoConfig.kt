package com.bithumbsystems.cms.api.config.mongo

import com.bithumbsystems.cms.api.config.aws.ParameterStoreConfig
import com.bithumbsystems.cms.api.util.Logger
import com.mongodb.ConnectionString
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.data.mapping.model.SnakeCaseFieldNamingStrategy
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.ReactiveMongoTransactionManager
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.data.mongodb.core.convert.NoOpDbRefResolver
import org.springframework.data.mongodb.core.mapping.MongoMappingContext
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.reactive.TransactionalOperator

@Configuration
@EnableReactiveMongoRepositories("com.bithumbsystems.cms.persistence.mongo")
@EnableTransactionManagement
@Order(Ordered.LOWEST_PRECEDENCE)
class MongoConfig(
    val parameterStoreConfig: ParameterStoreConfig
) : AbstractReactiveMongoConfiguration() {

    private val logger by Logger()

    override fun getDatabaseName() = parameterStoreConfig.mongoProperties.mongodbName

    @Bean
    override fun reactiveMongoDbFactory(): ReactiveMongoDatabaseFactory {
        return SimpleReactiveMongoDatabaseFactory(getConnectionString(parameterStoreConfig.mongoProperties))
    }

    @Bean
    override fun reactiveMongoTemplate(
        databaseFactory: ReactiveMongoDatabaseFactory,
        mongoConverter: MappingMongoConverter
    ): ReactiveMongoTemplate = ReactiveMongoTemplate(databaseFactory, mongoConverter)

    private fun getConnectionString(mongoProperties: MongoProperties): ConnectionString {
        logger.info(
            "mongodb://${mongoProperties.mongodbUser}:${mongoProperties.mongodbPassword}" +
                "@${mongoProperties.mongodbUri}:${mongoProperties.mongodbPort}/$databaseName?authSource=$databaseName"
        )
        return ConnectionString(
            "mongodb://${mongoProperties.mongodbUser}:${mongoProperties.mongodbPassword}" +
                "@${mongoProperties.mongodbUri}:${mongoProperties.mongodbPort}/$databaseName?authSource=$databaseName"
        )
    }

    @Bean
    @Primary
    override fun mappingMongoConverter(
        databaseFactory: ReactiveMongoDatabaseFactory,
        customConversions: MongoCustomConversions,
        mappingContext: MongoMappingContext
    ): MappingMongoConverter {
        mappingContext.setFieldNamingStrategy(SnakeCaseFieldNamingStrategy())
        mappingContext.isAutoIndexCreation = true
        val converter = MappingMongoConverter(NoOpDbRefResolver.INSTANCE, mappingContext)
        converter.setCustomConversions(customConversions)
        converter.setCodecRegistryProvider(databaseFactory)
        converter.setTypeMapper(DefaultMongoTypeMapper(null))
        return converter
    }

    @Bean
    fun transactionManager(factory: ReactiveMongoDatabaseFactory): ReactiveMongoTransactionManager {
        return ReactiveMongoTransactionManager(factory)
    }

    @Bean
    fun transactionOperator(manager: ReactiveTransactionManager): TransactionalOperator {
        return TransactionalOperator.create(manager)
    }
}
