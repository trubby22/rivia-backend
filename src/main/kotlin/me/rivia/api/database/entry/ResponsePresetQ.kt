package me.rivia.api.database.entry

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey


@DynamoDbBean
internal class ResponsePresetQ(@get:DynamoDbPartitionKey var presetQIdMeetingId: String?, var numSubmitted: Int?, var numSelected: Int?) {
    constructor() : this(null, null, null)
    constructor(presetQId: String, meetingId: String, numSubmitted: Int?, numSelected: Int?) : this("$presetQId $meetingId", numSubmitted, numSelected)
}

internal var ResponsePresetQ.presetQId
    get() = this.presetQIdMeetingId?.subSequence(0, this.presetQIdMeetingId!!.indexOf(' '))
        ?.toString()
    set(presetQId) {
        this.presetQIdMeetingId = "$presetQId $meetingId"
    }

internal var ResponsePresetQ.meetingId
    get() = this.presetQIdMeetingId?.subSequence(
        this.presetQIdMeetingId!!.indexOf(' '), this.presetQIdMeetingId!!.length
    )?.toString()
    set(meetingId) {
        this.presetQIdMeetingId = "$presetQId $meetingId"
    }
