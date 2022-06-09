package me.rivia.api.handlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.TableSchema

class GetDashboard: RequestHandler<GetDashboard.ApiContext, List<Meeting>> {
    class ApiContext(var cookie: Int? = null)

    val enhancedClient: DynamoDbEnhancedClient =
        DynamoDbEnhancedClient.create()
    override fun handleRequest(input: ApiContext?, context: Context?): List<Meeting> {
        val table: DynamoDbTable<Meeting> = enhancedClient
            .table("Meeting", TableSchema.fromBean(Meeting::class.java))

        return table.scan().items().toList()
    }
}