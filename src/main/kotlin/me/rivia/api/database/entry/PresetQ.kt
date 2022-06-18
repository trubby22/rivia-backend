package me.rivia.api.database.entry

import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType
import software.amazon.awssdk.enhanced.dynamodb.internal.converter.attribute.AtomicBooleanAttributeConverter
import software.amazon.awssdk.enhanced.dynamodb.internal.converter.attribute.BooleanAttributeConverter
import software.amazon.awssdk.enhanced.dynamodb.internal.converter.attribute.StringAttributeConverter
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
@DynamoDbBean
data class PresetQ(
    @get:DynamoDbPartitionKey
    var presetQId: String?,
    var text: String?,
    var isdefault: Boolean?
) {
    constructor() : this(null, null, null)
}
