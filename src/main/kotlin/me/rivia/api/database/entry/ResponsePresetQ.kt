package me.rivia.api.database.entry

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey


@DynamoDbBean
class ResponsePresetQ(@get:DynamoDbPartitionKey var presetQIdMeetingId: String?, var numSubmitted: Int?, var numSelected: Int?) {
    constructor() : this(null, null, null)
    constructor(presetQId: String, meetingId: String, numSubmitted: Int?, numSelected: Int?) : this("$presetQId $meetingId", numSubmitted, numSelected)
}

var ResponsePresetQ.presetQId
    get() = this.presetQIdMeetingId?.subSequence(0, this.presetQIdMeetingId!!.indexOf(' '))
        ?.toString()
    set(presetQId) {
        this.presetQIdMeetingId = "$presetQId $meetingId"
    }

var ResponsePresetQ.meetingId
    get() = this.presetQIdMeetingId?.subSequence(
        this.presetQIdMeetingId!!.indexOf(' ') + 1, this.presetQIdMeetingId!!.length
    )?.toString()
    set(meetingId) {
        this.presetQIdMeetingId = "$presetQId $meetingId"
    }
