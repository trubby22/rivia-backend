package me.rivia.api.handlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.TableSchema

class GetSummary : RequestHandler<GetSummary.ApiContext, List<Review>> {
    class ApiContext(var meeting_id: Uid? = null, var cookie: Int? = null)
    override fun handleRequest(
        input: ApiContext?,
        context: Context?
    ): List<Review> {
        val enhancedClient: DynamoDbEnhancedClient =
            DynamoDbEnhancedClient.create()
        val table: DynamoDbTable<Review> = enhancedClient
            .table("Review", TableSchema.fromBean(Review::class.java))

        return table.scan().items().toList()
    }
}
