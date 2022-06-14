package me.rivia.api.database.entry

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey

@DynamoDbBean
internal class ResponseUser(@get:DynamoDbPartitionKey var tenantIduserIdMeetingId: String?) {
    @get:DynamoDbPartitionKey
    internal var tenantIduserIdMeetingId

    constructor(tenantId: String, userId: String, meetingId: String) : this("$tenantId $userId $meetingId")
}

internal var ResponseUser.tenantId
    get() = this.tenantIduserIdMeetingId.subSequence(0, this.tenantIduserIdMeetingId.indexOf(' ')).toString()
    set(tenantId) {
        this.tenantIduserIdMeetingId = "$tenantId $userId $meetingId"
    }

internal var ResponseUser.userId: String
    get() {
        val firstSpace = this.tenantIduserIdMeetingId.indexOf(' ')
        val secondSpace = this.tenantIduserIdMeetingId.indexOf(' ', firstSpace + 1)
        return this.tenantIduserIdMeetingId.subSequence(firstSpace + 1, secondSpace).toString()
    }
    set(userId) {
        this.tenantIduserIdMeetingId = "$tenantId $userId $meetingId"
    }

internal var ResponseUser.meetingId : String
    get() {
        val firstSpace = this.tenantIduserIdMeetingId.indexOf(' ')
        val secondSpace = this.tenantIduserIdMeetingId.indexOf(' ', firstSpace + 1)
        return this.tenantIduserIdMeetingId.subSequence(secondSpace + 1, this.tenantIduserIdMeetingId.length).toString()
    }
    set(meetingId) {
        this.tenantIduserIdMeetingId = "$tenantId $userId $meetingId"
    }

