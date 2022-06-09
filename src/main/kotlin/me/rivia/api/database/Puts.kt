package me.rivia.api.database


import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import software.amazon.awssdk.services.dynamodb.model.ReturnValue

fun putEntry(
    tableName: String,
    primaryKeyName: String,
    attributes: Map<String, AttributeValue>
): Boolean {
    val request = PutItemRequest.builder().tableName(tableName).item(attributes).returnValues(ReturnValue.ALL_NEW)
        .conditionExpression("attribute_not_exists($primaryKeyName)").build()
    return dbClient.putItem(request)

}
