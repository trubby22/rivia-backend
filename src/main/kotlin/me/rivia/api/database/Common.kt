package me.rivia.api.database

import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient

internal val dbClient : DynamoDbClient = DynamoDbClient.builder()
    .region(Region.EU_WEST_2)
    .build()

private const val SINGLE_BATCH_LIMIT = 100
