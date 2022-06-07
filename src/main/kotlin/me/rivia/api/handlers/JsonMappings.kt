package me.rivia.api.handlers

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey

typealias Uid = String

class PresetQuestion(preset_q_id: Uid?, val preset_q_text: String?) {
    var preset_q_id = preset_q_id
        @DynamoDbPartitionKey get
        @DynamoDbPartitionKey set

    constructor() : this(null, null)
}
class Participant(participant_id: Uid?, val name: String?, val surname: String?, val email: String?) {
    var participant_id = participant_id
        @DynamoDbPartitionKey get
        @DynamoDbPartitionKey set
}
class Meeting(val title: String?, val start: Int?, val end: Int?) {
        constructor() : this(null, null, null)
}
class Response(val participant: Participant?, val quality: Float?, val preset_qs: ArrayList<String>?, val not_needed: ArrayList<Participant>?, val not_prepared: ArrayList<Participant>?) {
    constructor() : this(null, null, null, null, null)
}
