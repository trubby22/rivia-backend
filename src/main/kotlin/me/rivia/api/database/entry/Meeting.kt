package me.rivia.api.database.entry

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey

@DynamoDbBean
data class Meeting(
    @get:DynamoDbPartitionKey var meetingId: String?,
    var presetQIds: List<String>?,
    var organizerId: String?,
    var participantIds: List<String>?,
    var title: String?,
    var startTime: Int?,
    var endTime: Int?,
    var responsesCount: Int?,
    var qualities: List<Double>?,
    var feedbacks: List<String>?
) {
    constructor() : this(null, null, null, null, null, null, null, null, null, null)
}
