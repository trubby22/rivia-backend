package me.rivia.api.database.entry

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey


@DynamoDbBean
data class ResponsePresetQ(
    @get:DynamoDbPartitionKey var presetQIdMeetingId: String?,
    var numSubmitted: Int?,
    var numSelected: Int?
) {
    companion object {
        fun constructKey(presetQId: String?, meetingId: String?) = "${presetQId!!} ${meetingId!!}"
    }

    constructor() : this(null, null, null)
    constructor(
        presetQId: String?, meetingId: String?, numSubmitted: Int?, numSelected: Int?
    ) : this(
        constructKey(presetQId, meetingId), numSubmitted, numSelected
    )
}

var ResponsePresetQ.presetQId
    get() = this.presetQIdMeetingId?.split(' ')?.get(0)
    set(presetQId) {
        this.presetQIdMeetingId = ResponsePresetQ.constructKey(presetQId, meetingId)
    }
var ResponsePresetQ.meetingId
    get() = this.presetQIdMeetingId?.split(' ')?.get(1)
    set(meetingId) {
        this.presetQIdMeetingId = ResponsePresetQ.constructKey(presetQId, meetingId)
    }
