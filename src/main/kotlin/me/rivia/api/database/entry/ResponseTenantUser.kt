package me.rivia.api.database.entry

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey

@DynamoDbBean
internal class ResponseTenantUser(@get:DynamoDbPartitionKey var tenantIduserIdMeetingId: String?) {
    constructor() : this(null)
    constructor(tenantId: String, userId: String, meetingId: String) : this("$tenantId $userId $meetingId")
}

internal var ResponseTenantUser.tenantId
    get() = this.tenantIduserIdMeetingId?.subSequence(0, this.tenantIduserIdMeetingId!!.indexOf(' ')).toString()
    set(tenantId) {
        this.tenantIduserIdMeetingId = "$tenantId $userId $meetingId"
    }

internal var ResponseTenantUser.userId: String?
    get() {
        val firstSpace = this.tenantIduserIdMeetingId?.indexOf(' ') ?: return null
        val secondSpace = this.tenantIduserIdMeetingId!!.indexOf(' ', firstSpace + 1)
        return this.tenantIduserIdMeetingId!!.subSequence(firstSpace + 1, secondSpace).toString()
    }
    set(userId) {
        this.tenantIduserIdMeetingId = "$tenantId $userId $meetingId"
    }

internal var ResponseTenantUser.meetingId : String?
    get() {
        val firstSpace = this.tenantIduserIdMeetingId?.indexOf(' ') ?: return null
        val secondSpace = this.tenantIduserIdMeetingId!!.indexOf(' ', firstSpace + 1)
        return this.tenantIduserIdMeetingId!!.subSequence(secondSpace + 1, this.tenantIduserIdMeetingId!!.length).toString()
    }
    set(meetingId) {
        this.tenantIduserIdMeetingId = "$tenantId $userId $meetingId"
    }

