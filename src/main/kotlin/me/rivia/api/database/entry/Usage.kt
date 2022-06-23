package me.rivia.api.database.entry

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey

@DynamoDbBean
data class Usage(@get:DynamoDbPartitionKey var usageId: String?, var tenantId: String?, var userId: String?, var timings: List<Double>?) {
    constructor() : this(null, null, null, null)
}
