package me.rivia.api.database


import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.Expression
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException

inline fun <reified EntryType : DbEntry> putEntry(
    table: Table,
    entry: EntryType
): Boolean {
    val dynamoTable = dbEnhancedClient!!.table(
        table.toString(), TableSchema.fromClass
            (EntryType::class.java)
    )
    val noOverwriteExpression =
        Expression.builder().expression("attribute_not_exists(#primaryKey)")
            .putExpressionName("#primaryKey", entry.primaryKeyName()).build();
    val request =
        PutItemEnhancedRequest.builder(EntryType::class.java).item(entry)
            .conditionExpression(
                noOverwriteExpression
            ).build()
    try {
        dynamoTable.putItem(request)
    } catch (e: ConditionalCheckFailedException) {
        return false
    }
    return true
}

inline fun <reified EntryType> removeEntry(
    table: Table,
    keyValue: String,
) {
    val dynamoTable: DynamoDbTable<EntryType> =
        dbEnhancedClient!!.table(table.toString(), TableSchema.fromClass(EntryType::class.java))
    dynamoTable.deleteItem(Key.builder().partitionValue(keyValue).build())
}

inline fun <reified EntryType : DbEntry> updateEntry(
    table: Table,
    entry: EntryType,
    oldFieldName: String,
    oldFieldValue: AttributeValue,
): Boolean {
    val dynamoTable = dbEnhancedClient!!.table(
        table.toString(), TableSchema.fromClass
            (EntryType::class.java)
    )
    val noUnexpectedUpdateExpression =
        Expression.builder().expression("#fieldName = :fieldValue")
            .putExpressionName("#fieldName", oldFieldName).putExpressionValue(":fieldValue", oldFieldValue).build();
    val request =
        PutItemEnhancedRequest.builder(EntryType::class.java).item(entry)
            .conditionExpression(
                noUnexpectedUpdateExpression
            ).build()
    try {
        dynamoTable.putItem(request)
    } catch (e: ConditionalCheckFailedException) {
        return false
    }
    return true
}
