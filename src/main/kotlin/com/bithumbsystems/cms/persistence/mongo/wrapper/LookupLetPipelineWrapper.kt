package com.bithumbsystems.cms.persistence.mongo.wrapper

import org.bson.Document
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext

class LookupLetPipelineWrapper(
    private val lookup: Document,
    private val letOperation: Document,
    private val pipelineAggregation: Aggregation,
) : AggregationOperation {
    @Deprecated("Deprecated in Java")
    override fun toDocument(context: AggregationOperationContext): Document = lookup

    override fun toPipelineStages(context: AggregationOperationContext): List<Document> = run {
        listOf(lookup).apply {
            (this.iterator().next()[operator] as Document).apply {
                this.append("let", letOperation)
                this.append(
                    "pipeline",
                    pipelineAggregation.pipeline.operations.map { it.toPipelineStages(context).first() }
                )
            }
        }
    }
}
