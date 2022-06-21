package me.rivia.api.database.entry

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey

@DynamoDbBean
data class User(
    @get:DynamoDbPartitionKey var tenantIdUserId: String?, var name: String?, var surname: String?, var meetingIds: List<String>?
) {
    companion object {
        fun constructKey(tenantId: String?, userId: String?) = "${tenantId!!} ${userId!!}"
    }

    constructor() : this(null, null, null, null)
    constructor(tenantId: String?, userId: String?, name: String?, surname: String?, meetingIds: List<String>?) : this(
        constructKey(tenantId, userId), name, surname, meetingIds
    )

    var tenantId
        get() = tenantIdUserId?.split(' ')?.get(0)
        set(tenantId) {
            this.tenantIdUserId = constructKey(tenantId, userId)
        }
    var userId
        get() = tenantIdUserId?.split(' ')?.get(1)
        set(userId) {
            this.tenantIdUserId = constructKey(tenantId, userId)
        }
}
