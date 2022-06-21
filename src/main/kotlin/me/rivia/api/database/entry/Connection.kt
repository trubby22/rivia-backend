package me.rivia.api.database.entry

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey

@DynamoDbBean
data class Connection(@get:DynamoDbPartitionKey var tenantIdUserId: String?, var connectionIds: List<String>?) {
    constructor() : this(null, null)
    constructor(tenantId: String, userId: String, connectionIds: List<String>?) : this("$tenantId $userId", connectionIds)

    var tenantId
        get() = this.tenantIdUserId?.split(' ')?.get(1)
        set(tenantId) {
            this.tenantIdUserId = "$tenantId $userId"
        }
    var userId
        get() = this.tenantIdUserId?.split(' ')?.get(1)
        set(userId) {
            this.tenantIdUserId = "${tenantId!!} ${userId!!}"
        }
}
