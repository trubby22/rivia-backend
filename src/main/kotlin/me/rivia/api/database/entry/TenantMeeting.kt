package me.rivia.api.database.entry

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey

@DynamoDbBean
internal class TenantMeeting(@get:DynamoDbPartitionKey var tenantIdMeetingId: String?) {
    constructor() : this(null)
    constructor(tenantId: String, meetingId: String) : this("$tenantId $meetingId")
}

internal var TenantMeeting.tenantId
    get() = this.tenantIdMeetingId?.subSequence(0, this.tenantIdMeetingId!!.indexOf(' '))
        ?.toString()
    set(tenantId) {
        this.tenantIdMeetingId = "$tenantId $meetingId"
    }

internal var TenantMeeting.meetingId
    get() = this.tenantIdMeetingId?.subSequence(
        this.tenantIdMeetingId!!.indexOf(' '), this.tenantIdMeetingId!!.length
    )?.toString()
    set(meetingId) {
        this.tenantIdMeetingId = "$tenantId $meetingId"
    }
