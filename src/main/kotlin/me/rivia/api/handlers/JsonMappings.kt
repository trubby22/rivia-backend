package me.rivia.api.handlers

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey

typealias Uid = String

class PresetQuestion(var preset_q_id: Uid?, var preset_q_text: String?) {
    constructor() : this(null, null)
}

@DynamoDbBean
data class Participant(
    @get: DynamoDbPartitionKey
    var participant_id: Uid? = null,
    var name: String? = null,
    var surname: String? = null,
    var email: String? = null,
)

@DynamoDbBean
data class Meeting(
    @get:DynamoDbPartitionKey
    var meetingId: String? = null,
    var title: String? = null,
    var organisation: String? = null,
    var participants: Set<String>? = null,
    var reviews: Set<String>? = null,
    var startTime: Int? = null,
    var endTime: Int? = null,
)

//@DynamoDbBean
//data class HttpResponse(
//    @get:DynamoDbPartitionKey
//    var meeting: Meeting? = null,
//    var responses: List<Response>? = null,
//    var response_type: Int? = 2,
//)

@DynamoDbBean
data class Response(
    @get:DynamoDbPartitionKey
    var reviewId: String? = null,
    var userId: String? = null,
    var feedback: String? = null,
    var notNeeded: List<String>? = null,
    var notPrepared: List<String>? = null,
    var presetQ: List<String>? = null,
    var quality: Float? = null,
)
