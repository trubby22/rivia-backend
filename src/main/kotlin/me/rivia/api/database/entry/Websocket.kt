package me.rivia.api.database.entry

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey

@DynamoDbBean
data class Websocket(@get:DynamoDbPartitionKey var connectionId: String?, var tenantId: String?, var userId: String?) {
    constructor() : this(null, null, null)
}
