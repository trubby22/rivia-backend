package me.rivia.api.database

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.model.ReadBatch

inline fun <reified EntryType> getEntry(
    tableName: String,
    keyValue: String,
): EntryType {
    val table: DynamoDbTable<EntryType> = dbEnhancedClient
        .table(tableName, TableSchema.fromClass(EntryType::class.java))
    return table.getItem(Key.builder().partitionValue(keyValue).build())
}

//fun getEntryIndex(
//    tableName: String,
//    indexName: String,
//    indexValue: String
//): Map<String, AttributeValue>? {
//
//}

inline fun <reified EntryType> fetchSingleBatch(
    tableName: String,
    keys: List<String>,
): List<EntryType> {
    val table: DynamoDbTable<EntryType> = dbEnhancedClient
        .table(tableName, TableSchema.fromClass(EntryType::class.java))
    return dbEnhancedClient.batchGetItem { r ->
        r.addReadBatch(
            ReadBatch
                .builder(EntryType::class.java)
                .mappedTableResource(table)
                .apply {
                    repeat(keys.size) { index ->
                        this.addGetItem(
                            Key.builder().partitionValue
                                (keys[index]).build()
                        )
                    }
                }
                .build()
        )
    }.resultsForTable(table).toList()
}

inline fun <reified EntryType> getEntries(
    tableName: String,
    keys: Iterable<String>,
): ArrayList<EntryType> {
    val currentBatchKeys: MutableList<String> = mutableListOf()
    val results: MutableList<EntryType> = mutableListOf()
    for (key in keys) {
        currentBatchKeys.add(key)
        if (currentBatchKeys.size > SINGLE_BATCH_LIMIT) {
            results.addAll(fetchSingleBatch(tableName, currentBatchKeys))
        }
    }
    results.addAll(fetchSingleBatch(tableName, currentBatchKeys))
    return ArrayList(results)
}

inline fun <reified EntryType> getAllEntries(
    tableName: String
): ArrayList<EntryType> {
    val table: DynamoDbTable<EntryType> = dbEnhancedClient
        .table(tableName, TableSchema.fromClass(EntryType::class.java))
    return ArrayList(table.scan().items().toList())
}

class PutError(tableName: String) :
    Error("Failed to put item into'$tableName")

