    package me.rivia.api

    import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
    import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey

    typealias Uid = String
@DynamoDbBean
class PresetQuestion(preset_q_id: Uid?, val preset_q_text: String?) {
    var preset_q_id = preset_q_id
        @DynamoDbPartitionKey get
        @DynamoDbPartitionKey set
    constructor() : this(null, null)
}@DynamoDbBean
class Participant(user_id: Uid?, val name: String?, val surname: String?, val email: String?) {
        var user_id = user_id
            @DynamoDbPartitionKey get
            @DynamoDbPartitionKey set

        constructor() : this(null, null, null, null)
}
data class Meeting(val title: String?, val start: Int?, val end: Int?) {
    constructor() : this(null, null, null)
}
class Response(val participant: Participant?, val quality: Float?, val preset_qs: Array<String>?, val not_needed: Array<Participant>?, val not_prepared: Array<Participant>?) {
    constructor() : this(null, null, null, null, null)
}
