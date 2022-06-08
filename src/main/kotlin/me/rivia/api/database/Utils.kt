package me.rivia.api.database

import me.rivia.api.handlers.Meeting
import me.rivia.api.handlers.Participant
import me.rivia.api.handlers.PostCreateAccount
import me.rivia.api.handlers.PresetQuestion
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.BatchGetItemRequest
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.KeysAndAttributes
import java.util.LinkedList
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.lambda.runtime.Context
import java.util.UUID


val dbClient : DynamoDbClient = DynamoDbClient.builder()
    .region(Region.EU_WEST_2)
    .build()

val client: AmazonDynamoDB = AmazonDynamoDBClientBuilder
    .standard()
    .withRegion(Regions.EU_WEST_2)
    .build()
val db = DynamoDB(client)

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
class PutError(tableName : String) : Error("Failed to put item into'$tableName")

inline fun getMeeting(meetingEntry : Map<String, AttributeValue>) = Meeting(
        meetingEntry["title"]?.s() ?: throw FieldError("Meeting", "title"),
        meetingEntry["startTime"]?.n()?.toInt() ?: throw FieldError("Meeting", "startTime"),
        meetingEntry["endTime"]?.n()?.toInt() ?: throw FieldError("Meeting", "endTime"),
    )

inline fun getParticipant(participantEntry : Map<String, AttributeValue>) = Participant(
    participantEntry.get("UserID")?.s() ?: throw FieldError("User", "UserID"),
    participantEntry["name"]?.s() ?: throw FieldError("User", "name"),
    participantEntry["surname"]?.s() ?: throw FieldError("User", "surname"),
    participantEntry["email"]?.s() ?: throw FieldError("User", "email")
)

inline fun getPresetQ(presetQEntry: Map<String, AttributeValue>) = PresetQuestion(
    presetQEntry["PresetQID"]?.s() ?: throw FieldError("PresetQs", "PresetQID"),
    presetQEntry["text"]?.s() ?: throw FieldError("PresetQs", "text")
)

inline fun putAccount(accountData : PostCreateAccount.Companion.AccountData) {
    // should this be already hashed
    val table = db.getTable("User")
    // can change this to use enhanced database
    val item = Item()
        .withString("UserID", UUID.randomUUID().toString())
        .withString("name", accountData.name)
        .withString("surname", accountData.surname)
        .withString("email", accountData.email)
        .withString("password", accountData.password)
    val outcome = table.putItem(item) ?: throw PutError("User")
}

//inline fun putParticipant()

//inline fun putPresetQ()

