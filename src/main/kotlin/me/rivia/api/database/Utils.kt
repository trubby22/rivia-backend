package me.rivia.api.database

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.model.BatchGetItemEnhancedRequest
import software.amazon.awssdk.enhanced.dynamodb.model.ReadBatch
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient

private val simpleClient = DynamoDbClient.builder()
    .region(Region.EU_WEST_2)
    .build()

val enhancedClient = DynamoDbEnhancedClient.builder().dynamoDbClient(simpleClient).build()

inline fun <reified TableEntryType> getTable(tableName: String) =
    enhancedClient.table(tableName, TableSchema.fromBean(TableEntryType::class.java))

inline fun <reified TableEntryType> getEntry(
    tableName: String,
    primaryKey: String
): TableEntryType? {
    val table = getTable<TableEntryType>(tableName)
    val key = Key.builder().partitionValue(primaryKey).build()
    return table.getItem(key)
}

inline fun <reified TableEntryType> getEntries(
    tableName: String,
    primaryKeys: Iterable<String>
): List<TableEntryType> {
    val table = getTable<TableEntryType>(tableName)
    var batchGetItemBuilder = BatchGetItemEnhancedRequest.builder()
    for (primaryKey in primaryKeys) {
        val key = Key.builder().partitionValue(primaryKey).build()
        val readBatch = ReadBatch.builder(TableEntryType::class.java).mappedTableResource(table).addGetItem(key).build()
        batchGetItemBuilder = batchGetItemBuilder.addReadBatch(readBatch)
    }
    return enhancedClient.batchGetItem(batchGetItemBuilder.build())
}
