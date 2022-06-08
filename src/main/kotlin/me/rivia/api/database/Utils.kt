package me.rivia.api.database

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
    primaryKey: String
): Map<String, AttributeValue>? {
    val key = mapOf(primaryKeyName to AttributeValue.fromS(primaryKey))
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
    primaryKeys: Iterable<String>
): List<Map<String, AttributeValue>> {
    val primaryKeyMaps = LinkedList<Map<String, AttributeValue>>();
    val responses = LinkedList<Map<String, AttributeValue>>()
    for (primaryKey in primaryKeys) {
        primaryKeyMaps.add(mapOf(primaryKeyName to AttributeValue.fromS(primaryKey)))
        if (primaryKeyMaps.size == SINGLE_BATCH_LIMIT) {
            responses.addAll(fetchSingleBatch(tableName, primaryKeyMaps))
        }
    }
    responses.addAll(fetchSingleBatch(tableName, primaryKeyMaps))
    return responses
}
