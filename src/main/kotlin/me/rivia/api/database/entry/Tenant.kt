package me.rivia.api.database.entry

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey

@DynamoDbBean
data class Tenant(
    @get:DynamoDbPartitionKey
    var tenantId: String?,
    var applicationRefreshToken: String?,
    var applicationAccessToken: String?,
    var userRefreshToken: String?,
    var userAccessToken: String?,
    var presetQIds: List<String>?,
    var meetingIds: List<String>?, // for listing the meetings in the organization
) {
    constructor() : this(null,null,null,null, null, null, null)
}
