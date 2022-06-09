package me.rivia.api.database

import me.rivia.api.handlers.Meeting
import me.rivia.api.handlers.Participant
import me.rivia.api.handlers.PresetQuestion
import me.rivia.api.handlers.Response
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.model.ReadBatch
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.BatchGetItemRequest
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.KeysAndAttributes
import java.util.LinkedList

inline fun <reified EntryType> getEntry(
    tableName: String,
    primaryKeyValue: String,
): EntryType {
    val table: DynamoDbTable<EntryType> = dbEnhancedClient
        .table(tableName, TableSchema.fromClass(EntryType::class.java))
    return table.getItem(Key.builder().partitionValue(primaryKeyValue).build())
}

//fun getEntryIndex(
//    tableName: String,
//    indexName: String,
//    indexValue: String
//): Map<String, AttributeValue>? {
//
//}

private inline fun <reified EntryType> fetchSingleBatch(
    tableName: String,
    primaryKeys: List<String>,
): List<EntryType> {
    val table: DynamoDbTable<EntryType> = dbEnhancedClient
        .table(tableName, TableSchema.fromClass(EntryType::class.java))
    return dbEnhancedClient.batchGetItem { r ->
        r.addReadBatch(
            ReadBatch
                .builder(EntryType::class.java)
                .mappedTableResource(table)
                .apply {
                    repeat(primaryKeys.size) { index ->
                        this.addGetItem(
                            Key.builder().partitionValue
                                (primaryKeys[index]).build()
                        )
                    }
                }
                .build()
        )
    }.resultsForTable(table).toList()
}

fun getEntries(
    tableName: String,
    primaryKeyName: String,
    primaryKeyValues: Iterable<String>
): List<Map<String, AttributeValue>> {
    val primaryKeyMaps = LinkedList<Map<String, AttributeValue>>()
    val responses = LinkedList<Map<String, AttributeValue>>()
    for (primaryKey in primaryKeyValues) {
        primaryKeyMaps.add(
            mapOf(
                primaryKeyName to AttributeValue.fromS(
                    primaryKey
                )
            )
        )
        if (primaryKeyMaps.size > SINGLE_BATCH_LIMIT) {
            responses.addAll(fetchSingleBatch(tableName, primaryKeyMaps))
        }
    }
    responses.addAll(fetchSingleBatch(tableName, primaryKeyMaps))
    return responses
}

class FieldError(tableName: String, field: String) :
    Error("'$field' field of the '$tableName' table not present")

class PutError(tableName: String) :
    Error("Failed to put item into'$tableName")

fun getMeeting(meetingEntry: Map<String, AttributeValue>) = Meeting(
    meetingEntry["title"]?.s() ?: throw FieldError("Meeting", "title"),
    meetingEntry["startTime"]?.n()?.toInt() ?: throw FieldError(
        "Meeting",
        "startTime"
    ),
    meetingEntry["endTime"]?.n()?.toInt() ?: throw FieldError(
        "Meeting",
        "endTime"
    ),
)

fun getParticipant(participantEntry: Map<String, AttributeValue>) =
    Participant(
        participantEntry.get("UserID")?.s() ?: throw FieldError(
            "User",
            "UserID"
        ),
        participantEntry["name"]?.s() ?: throw FieldError("User", "name"),
        participantEntry["surname"]?.s() ?: throw FieldError(
            "User",
            "surname"
        ),
        participantEntry["email"]?.s() ?: throw FieldError("User", "email")
    )

fun getPresetQ(presetQEntry: Map<String, AttributeValue>) = PresetQuestion(
    presetQEntry["PresetQID"]?.s() ?: throw FieldError(
        "PresetQs",
        "PresetQID"
    ),
    presetQEntry["text"]?.s() ?: throw FieldError("PresetQs", "text")
)

