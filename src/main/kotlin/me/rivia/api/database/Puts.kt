package me.rivia.api.database


import software.amazon.awssdk.enhanced.dynamodb.Expression
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException

inline fun <reified EntryType : DbEntry> putEntry(
    tableName: String,
    entry: EntryType
): Boolean {
    val table = dbEnhancedClient.table(tableName, TableSchema.fromClass(EntryType::class.java))
    val noOverwriteExpression = Expression.builder().expression("attribute_not_exists(#primaryKey)")
        .putExpressionName("#primaryKey", entry.primaryKeyName()).build();
    val request =
        PutItemEnhancedRequest.builder(EntryType::class.java).item(entry).conditionExpression(
            noOverwriteExpression
        ).build()
    try {
        table.putItem(request)
    } catch (e: ConditionalCheckFailedException) {
        return false
    }
    return true
}
