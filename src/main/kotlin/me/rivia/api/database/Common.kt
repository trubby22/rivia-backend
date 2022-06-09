package me.rivia.api.database

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

val dbClient: DynamoDbClient = DynamoDbClient.builder()
    .region(Region.EU_WEST_2)
    .build()

val dbEnhancedClient: DynamoDbEnhancedClient =
    DynamoDbEnhancedClient.builder().dynamoDbClient(dbClient).build()

interface DbEntry {
    fun primaryKeyName(): String
}

const val SINGLE_BATCH_LIMIT = 100

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
