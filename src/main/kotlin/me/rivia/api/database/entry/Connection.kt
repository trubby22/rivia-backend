package me.rivia.api.database.entry

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey

@DynamoDbBean
data class Connection(@get:DynamoDbPartitionKey var tenantIdUserId: String?, var connectionIds: List<String>?) {
    constructor() : this(null, null)
    constructor(tenantId: String, userId: String, connectionIds: List<String>?) : this("$tenantId $userId", connectionIds)
}

var Connection.tenantId
    get() = this.tenantIdUserId?.subSequence(0, this.tenantIdUserId!!.indexOf(' '))
        ?.toString()
    set(tenantId) {
        this.tenantIdUserId = "$tenantId $userId"
    }

var Connection.userId
    get() = this.tenantIdUserId?.subSequence(
        this.tenantIdUserId!!.indexOf(' ') + 1, this.tenantIdUserId!!.length
    )?.toString()
    set(userId) {
        this.tenantIdUserId = "$tenantId $userId"
    }
