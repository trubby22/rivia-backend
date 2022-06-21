package me.rivia.api.database.entry

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey

@DynamoDbBean
data class Opinion(
    @get:DynamoDbPartitionKey var opinionId: String?,
    var tenantId: String?,
    var userId: String?,
    var like: Double?,
    var use: Double?
) {
    constructor() : this(null, null, null, null, null)
}
