package me.rivia.api.database.entry

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey

@DynamoDbBean
data class ResponseDataUsers(
    @get:DynamoDbPartitionKey var tenantIdUserIdMeetingId: String?,
    var needed: Int?,
    var notNeeded: Int?,
    var prepared: Int?,
    var notPrepared: Int?
) {
    companion object {
        fun constructKey(tenantId: String?, userId: String?, meetingId: String?) =
            "${tenantId!!} ${userId!!} ${meetingId!!}"
    }

    constructor() : this(null, null, null, null, null)
    constructor(
        tenantId: String?,
        userId: String?,
        meetingId: String?,
        needed: Int?,
        notNeeded: Int?,
        prepared: Int?,
        notPrepared: Int?
    ) : this(constructKey(tenantId, userId, meetingId), needed, notNeeded, prepared, notPrepared)

    var tenantId
        get() = this.tenantIdUserIdMeetingId?.split(' ')?.get(0)
        set(tenantId) {
            this.tenantIdUserIdMeetingId = constructKey(tenantId, userId, meetingId)
        }
    var userId: String?
        get() = this.tenantIdUserIdMeetingId?.split(' ')?.get(1)
        set(userId) {
            this.tenantIdUserIdMeetingId = constructKey(tenantId, userId, meetingId)
        }
    var meetingId: String?
        get() = this.tenantIdUserIdMeetingId?.split(' ')?.get(2)
        set(meetingId) {
            this.tenantIdUserIdMeetingId = constructKey(tenantId, userId, meetingId)
        }
}
