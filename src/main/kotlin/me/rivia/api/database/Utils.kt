package me.rivia.api.database

import me.rivia.api.handlers.Meeting
import me.rivia.api.handlers.Participant
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.BatchGetItemRequest
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.KeysAndAttributes
import java.util.LinkedList

val dbClient : DynamoDbClient = DynamoDbClient.builder()
    .region(Region.EU_WEST_2)
    .build()

inline fun getEntry(
    tableName: String,
    primaryKeyName: String,
    primaryKeyValue: String
): Map<String, AttributeValue>? {
    val key = mapOf(primaryKeyName to AttributeValue.fromS(primaryKeyValue))
    val request = GetItemRequest.builder().tableName(tableName).key(key).build()
    return dbClient.getItem(request).item()
}

const val SINGLE_BATCH_LIMIT = 100
inline fun fetchSingleBatch(
    tableName: String,
    primaryKeyMaps: Collection<Map<String, AttributeValue>>,
): List<Map<String, AttributeValue>> {
    val keysAndAttributes = KeysAndAttributes.builder().keys(primaryKeyMaps).build()
    val request = BatchGetItemRequest.builder().requestItems(mapOf(tableName to keysAndAttributes)).build()
    return dbClient.batchGetItem(request).responses()?.get(tableName) ?: listOf()
}

inline fun getEntries(
    tableName: String,
    primaryKeyName: String,
    primaryKeyValues: Iterable<String>
): List<Map<String, AttributeValue>> {
    val primaryKeyMaps = LinkedList<Map<String, AttributeValue>>();
    val responses = LinkedList<Map<String, AttributeValue>>()
    for (primaryKey in primaryKeyValues) {
        primaryKeyMaps.add(mapOf(primaryKeyName to AttributeValue.fromS(primaryKey)))
        if (primaryKeyMaps.size == SINGLE_BATCH_LIMIT) {
            responses.addAll(fetchSingleBatch(tableName, primaryKeyMaps))
        }
    }
    responses.addAll(fetchSingleBatch(tableName, primaryKeyMaps))
    return responses
}

class FieldError(tableName: String, field: String) : Error("'$field' field of the '$tableName' table not present")

inline fun getMeeting(meetingEntry : Map<String, AttributeValue>) = Meeting(
        meetingEntry["title"]?.s() ?: throw FieldError("Meeting", "title"),
        meetingEntry["startTime"]?.n()?.toInt() ?: throw FieldError("Meeting", "startTime"),
        meetingEntry["endTime"]?.n()?.toInt() ?: throw FieldError("Meeting", "endTime"),
    )

inline fun getParticipant(participantEntry : Map<String, AttributeValue>) = Participant(
    participant_id = participantEntry.get("UserID")?.s() ?: throw FieldError("User", "UserID"),
    name = participantEntry["name"]?.s() ?: throw FieldError("User", "name"),
    surname = participantEntry["surname"]?.s() ?: throw FieldError("User", "surname"),
    email = participantEntry["email"]?.s() ?: throw FieldError("User", "email")
)

