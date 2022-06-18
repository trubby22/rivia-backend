package me.rivia.api.database.entry

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey

@DynamoDbBean
internal class ResponseTenantUser(@get:DynamoDbPartitionKey var tenantIdUserIdMeetingId: String?) {
    constructor() : this(null)
    constructor(tenantId: String, userId: String, meetingId: String) : this("$tenantId $userId $meetingId")
}

internal var ResponseTenantUser.tenantId
    get() = this.tenantIdUserIdMeetingId?.subSequence(0, this.tenantIdUserIdMeetingId!!.indexOf(' ')).toString()
    set(tenantId) {
        this.tenantIdUserIdMeetingId = "$tenantId $userId $meetingId"
    }

internal var ResponseTenantUser.userId: String?
    get() {
        val firstSpace = this.tenantIdUserIdMeetingId?.indexOf(' ') ?: return null
        val secondSpace = this.tenantIdUserIdMeetingId!!.indexOf(' ', firstSpace + 1)
        return this.tenantIdUserIdMeetingId!!.subSequence(firstSpace + 1, secondSpace).toString()
    }
    set(userId) {
        this.tenantIdUserIdMeetingId = "$tenantId $userId $meetingId"
    }

internal var ResponseTenantUser.meetingId : String?
    get() {
        val firstSpace = this.tenantIdUserIdMeetingId?.indexOf(' ') ?: return null
        val secondSpace = this.tenantIdUserIdMeetingId!!.indexOf(' ', firstSpace + 1)
        return this.tenantIdUserIdMeetingId!!.subSequence(secondSpace + 1, this.tenantIdUserIdMeetingId!!.length).toString()
    }
    set(meetingId) {
        this.tenantIdUserIdMeetingId = "$tenantId $userId $meetingId"
    }

