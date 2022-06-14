package me.rivia.api.database

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey

@DynamoDbBean
internal class Meeting(
    @get:DynamoDbPartitionKey
    var meetingId: String,
    var tenantId: String,
    var organizerId: String,
    var participantIds: List<String>,
    var title: String,
    var startTime: Int,
    var endTime: Int,
    var qualitySum: Double,
    var qualityCount: Int
)
