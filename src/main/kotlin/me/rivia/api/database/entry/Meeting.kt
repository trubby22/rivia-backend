package me.rivia.api.database.entry

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey

@DynamoDbBean
data class Meeting(
    @get:DynamoDbPartitionKey var meetingId: String?,
    var tenantId: String?,
    var organizerId: String?,
    var userIds: List<String>?,
    var title: String?,
    var startTime: Int?,
    var endTime: Int?,
    var responsesCount: Int?,
    var presetQIds: List<String>?,
    var qualities: List<Double>?,
    var feedbacks: List<String>?
) {
    constructor() : this(null, null, null, null, null, null, null, null, null, null, null)
}
