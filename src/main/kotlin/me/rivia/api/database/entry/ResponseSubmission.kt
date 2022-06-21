package me.rivia.api.database.entry

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey

@DynamoDbBean
data class ResponseSubmission(@get:DynamoDbPartitionKey var tenantIdUserIdMeetingId: String?) {
    companion object {
        fun constructKey(tenantId: String?, userId: String?, meetingId: String?) =
            "${tenantId!!} ${userId!!} ${meetingId!!}"
    }

    constructor() : this(null)
    constructor(tenantId: String, userId: String, meetingId: String) : this(
        constructKey(
            tenantId, userId, meetingId
        )
    )
}

var ResponseSubmission.tenantId
    get() = this.tenantIdUserIdMeetingId?.split(' ')?.get(0)
    set(tenantId) {
        this.tenantIdUserIdMeetingId = ResponseSubmission.constructKey(tenantId, userId, meetingId)
    }
var ResponseSubmission.userId: String?
    get() = this.tenantIdUserIdMeetingId?.split(' ')?.get(1)
    set(userId) {
        this.tenantIdUserIdMeetingId = ResponseSubmission.constructKey(tenantId, userId, meetingId)
    }
var ResponseSubmission.meetingId: String?
    get() = this.tenantIdUserIdMeetingId?.split(' ')?.get(2)
    set(meetingId) {
        this.tenantIdUserIdMeetingId = ResponseSubmission.constructKey(tenantId, userId, meetingId)
    }
