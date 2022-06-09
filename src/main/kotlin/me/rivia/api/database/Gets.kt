package me.rivia.api.database

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.model.ReadBatch

inline fun <reified EntryType> getEntry(
    table: Table,
    keyValue: String,
): EntryType? {
    val dynamoTable: DynamoDbTable<EntryType> = dbEnhancedClient
        .table(table.toString(), TableSchema.fromClass(EntryType::class.java))
    return dynamoTable.getItem(Key.builder().partitionValue(keyValue).build())
}

inline fun <reified EntryType> fetchSingleBatch(
    table: Table,
    keys: List<String>,
): List<EntryType> {
    val dynamoTable: DynamoDbTable<EntryType> = dbEnhancedClient
        .table(table.toString(), TableSchema.fromClass(EntryType::class.java))
    return dbEnhancedClient.batchGetItem { r ->
        r.addReadBatch(
            ReadBatch
                .builder(EntryType::class.java)
                .mappedTableResource(dynamoTable)
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
    }.resultsForTable(dynamoTable).toList()
}

inline fun <reified EntryType> getEntries(
    table: Table,
    keys: Iterable<String>,
): ArrayList<EntryType> {
    val currentBatchKeys: MutableList<String> = mutableListOf()
    val results: MutableList<EntryType> = mutableListOf()
    for (key in keys) {
        currentBatchKeys.add(key)
        if (currentBatchKeys.size > SINGLE_BATCH_LIMIT) {
            results.addAll(fetchSingleBatch(table, currentBatchKeys))
        }
    }
    results.addAll(fetchSingleBatch(table, currentBatchKeys))
    return ArrayList(results)
}

inline fun <reified EntryType> getAllEntries(
    table: Table,
): ArrayList<EntryType> {
    val dynamoTable: DynamoDbTable<EntryType> = dbEnhancedClient
        .table(table.toString(), TableSchema.fromClass(EntryType::class.java))
    return ArrayList(dynamoTable.scan().items().toList())
}

class PutError(tableName: String) :
    Error("Failed to put item into'$tableName")

