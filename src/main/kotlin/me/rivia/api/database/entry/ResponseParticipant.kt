package me.rivia.api.database.entry

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey

@DynamoDbBean
internal class ResponseParticipant(@get:DynamoDbPartitionKey var participantIdMeetingId: String?) {
    constructor() : this(null)
    constructor(participantId: String, meetingId: String) : this("$participantId $meetingId")
}

internal var ResponseParticipant.participantId
    get() = this.participantIdMeetingId?.subSequence(0, this.participantIdMeetingId!!.indexOf(' '))
        ?.toString()
    set(participantId) {
        this.participantIdMeetingId = "$participantId $meetingId"
    }

internal var ResponseParticipant.meetingId
    get() = this.participantIdMeetingId?.subSequence(
        this.participantIdMeetingId!!.indexOf(' '), this.participantIdMeetingId!!.length
    )?.toString()
    set(meetingId) {
        this.participantIdMeetingId = "$participantId $meetingId"
    }

