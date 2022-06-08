package me.rivia.api.handlers

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey

typealias Uid = String

class PresetQuestion(var preset_q_id: Uid?, var preset_q_text: String?) {
    constructor() : this(null, null)
}
class Participant(var participant_id: Uid?, var name: String?, var surname: String?, var email: String?) {
    constructor(): this(null, null, null, null)
}
class Meeting(val title: String?, val start: Int?, val end: Int?) {
        constructor() : this(null, null, null)
}
class Response(val participant: Participant?, val quality: Float?, val preset_qs: ArrayList<String>?, val not_needed: ArrayList<Participant>?, val not_prepared: ArrayList<Participant>?) {
    constructor() : this(null, null, null, null, null)
}
