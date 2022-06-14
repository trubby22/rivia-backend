package me.rivia.api.database

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey

internal class TenantMeeting(tenantId: String, meetingId: String) {
    @get:DynamoDbPartitionKey
    internal var tenantIdMeetingId = "$tenantId $meetingId"
}

internal var TenantMeeting.tenantId
    get() = this.tenantIdMeetingId.subSequence(0, this.tenantIdMeetingId.indexOf(' ')).toString()
    set(tenantId: String) {
        this.tenantIdMeetingId = "$tenantId ${this.meetingId}"
    }

internal var TenantMeeting.meetingId
    get() = this.tenantIdMeetingId.subSequence(this.tenantIdMeetingId.indexOf(' '), this.tenantIdMeetingId.length).toString()
    set(meetingId: String) {
        this.tenantIdMeetingId = "${this.tenantId} $meetingId"
    }
