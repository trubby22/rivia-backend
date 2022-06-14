package me.rivia.api.database.entry

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey

@DynamoDbBean
internal class Participant(
    @get:DynamoDbPartitionKey
    var participantId: String?,
    var name: String?,
    var surname: String?
) {
    constructor() : this(null,null,null)
}
