package me.rivia.api.database

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey

@DynamoDbBean
internal class PresetQ(
    @get:DynamoDbPartitionKey
    var presetQId: String,
    var text: String,
    var isDefault: Boolean
)
