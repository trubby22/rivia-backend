package me.rivia.api.database

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.model.ReadBatch

inline fun <reified EntryType> getEntry(
    table: Table,
    keyValue: String,
): EntryType? {
    val dynamoTable: DynamoDbTable<EntryType> =
        dbEnhancedClient!!.table(table.toString(), TableSchema.fromClass(EntryType::class.java))
    return dynamoTable.getItem(Key.builder().partitionValue(keyValue).build())
}

inline fun <reified EntryType> fetchSingleBatch(
    table: Table,
    keys: List<String>,
): List<EntryType> {
    val dynamoTable: DynamoDbTable<EntryType> =
        dbEnhancedClient!!.table(table.toString(), TableSchema.fromClass(EntryType::class.java))
    return dbEnhancedClient!!.batchGetItem { r ->
        r.addReadBatch(ReadBatch.builder(EntryType::class.java).mappedTableResource(dynamoTable)
            .apply {
                repeat(keys.size) { index ->
                    this.addGetItem(
                        Key.builder().partitionValue(keys[index]).build()
                    )
                }
            }.build())
    }.resultsForTable(dynamoTable).toList()
}

inline fun <reified EntryType> getEntries(
    table: Table,
    keys: Collection<String>,
): List<EntryType> {
    if (keys.isEmpty()) {
        return listOf()
    }
    val currentBatchKeys: MutableList<String> = mutableListOf()
    val results: MutableList<EntryType> = mutableListOf()
    for (key in keys) {
        currentBatchKeys.add(key)
        if (currentBatchKeys.size > SINGLE_BATCH_LIMIT) {
            results.addAll(fetchSingleBatch(table, currentBatchKeys))
        }
    }
    results.addAll(fetchSingleBatch(table, currentBatchKeys))
    return results
}

inline fun getUser(
    session: String?
): String? = getEntry<Session>(
    Table.SESSION, session ?: throw Error("Session not present")
)?.user

inline fun <reified EntryType> getAllEntries(
    table: Table,
): List<EntryType> {
    val dynamoTable: DynamoDbTable<EntryType> =
        dbEnhancedClient!!.table(table.toString(), TableSchema.fromClass(EntryType::class.java))
    return dynamoTable.scan().items().toList()
}

class PutError(tableName: String) : Error("Failed to put item into'$tableName")

