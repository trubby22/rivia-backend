package me.rivia.api.database.entry

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean

@DynamoDbBean
data class Usage(var usageId: String?, var tenantId: String?, var userId: String?, var timings: List<Float>?) {
    constructor() : this(null, null, null, null)
}
