package me.rivia.api.handlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.LambdaLogger
import com.amazonaws.services.lambda.runtime.RequestHandler
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional.keyEqualTo
import software.amazon.awssdk.enhanced.dynamodb.model.ReadBatch

class GetParticipants : RequestHandler<Unit, List<Participant>> {
    override fun handleRequest(
        input: Unit?,
        context: Context?
    ): List<Participant> {
        val enhancedClient: DynamoDbEnhancedClient =
            DynamoDbEnhancedClient.create()
        val table: DynamoDbTable<Participant> = enhancedClient
            .table("User", TableSchema.fromBean(Participant::class.java))

        return table.scan().items().toList()
    }

}