package me.rivia.api.database.entry

import me.rivia.api.handlers.PostSubscription
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey

@DynamoDbBean
data class Tenant(
    @get:DynamoDbPartitionKey
    var tenantId: String?,
    var subscriptionId: String?,
    var applicationRefreshToken: String?,
    var applicationAccessToken: String?,
    var refreshToken: String?,
    var accessToken: String?,
    var presetQIds: List<String>?,
) {
    constructor() : this(null,null , null,null,null, null, null)
}
