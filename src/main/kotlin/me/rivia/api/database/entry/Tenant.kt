package me.rivia.api.database.entry

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey

@DynamoDbBean
internal class Tenant(
    @get:DynamoDbPartitionKey
    var tenantId: String?,
    var refreshToken: String?,
    var presetQIds: List<String>?,
    var meetingIds: List<String>?, // for listing the meetings in the organization
) {
    constructor() : this(null,null,null,null)
}
