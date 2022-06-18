package me.rivia.api.database.entry

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey

@DynamoDbBean
data class ResponseParticipant(
    @get:DynamoDbPartitionKey var participantIdMeetingId: String?,
    var needed: Int?,
    var notNeeded: Int?,
    var prepared: Int?,
    var notPrepared: Int?
) {
    constructor() : this(null, null, null, null, null)
    constructor(
        participantId: String,
        meetingId: String,
        needed: Int?,
        notNeeded: Int?,
        prepared: Int?,
        notPrepared: Int?
    ) : this("$participantId $meetingId", needed, notNeeded, prepared, notPrepared)
}

var ResponseParticipant.participantId
    get() = this.participantIdMeetingId?.subSequence(0, this.participantIdMeetingId!!.indexOf(' '))
        ?.toString()
    set(participantId) {
        this.participantIdMeetingId = "$participantId $meetingId"
    }

var ResponseParticipant.meetingId
    get() = this.participantIdMeetingId?.subSequence(
        this.participantIdMeetingId!!.indexOf(' ') + 1, this.participantIdMeetingId!!.length
    )?.toString()
    set(meetingId) {
        this.participantIdMeetingId = "$participantId $meetingId"
    }

