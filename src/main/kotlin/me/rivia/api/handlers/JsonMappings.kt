package me.rivia.api.handlers

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey

typealias Uid = String

data class PresetQuestion(var preset_q_id: Uid?, var preset_q_text: String?) {
    constructor() : this(null, null)
}

data class Participant(
    var participant_id: Uid?,
    var name: String?,
    var surname: String?,
    var email: String?
) {
    constructor() : this(null, null, null, null)
}

data class Meeting(var title: String?, var start_time: Int?, var end_time: Int?) {
    constructor() : this(null, null, null)
}

class Response(
    var participant: Participant?,
    var quality: Float?,
    var preset_qs: ArrayList<String>?,
    var not_needed: ArrayList<Participant>?,
    var not_prepared: ArrayList<Participant>?
) {
    constructor() : this(null, null, null, null, null)
}
