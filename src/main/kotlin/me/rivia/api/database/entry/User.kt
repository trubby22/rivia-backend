package me.rivia.api.database.entry

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey

@DynamoDbBean
data class User(
    @get:DynamoDbPartitionKey var tenantIduserId: String?, var name: String?, var surname: String?
) {
    companion object {
        fun constructKey(tenantId: String?, userId: String?) = "${tenantId!!} ${userId!!}"
    }

    constructor() : this(null, null, null)
    constructor(tenantId: String?, userId: String?, name: String?, surname: String?) : this(
        constructKey(tenantId, userId), name, surname
    )

    var tenantId
        get() = tenantIduserId?.split(' ')?.get(0)
        set(tenantId) {
            this.tenantIduserId = constructKey(tenantId, userId)
        }
    var userId
        get() = tenantIduserId?.split(' ')?.get(1)
        set(userId) {
            this.tenantIduserId = constructKey(tenantId, userId)
        }
}
