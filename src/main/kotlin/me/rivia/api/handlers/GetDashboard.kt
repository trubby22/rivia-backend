package me.rivia.api.handlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import me.rivia.api.database.Table
import me.rivia.api.database.getAllEntries
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.TableSchema

class GetDashboard: RequestHandler<GetDashboard.ApiContext, List<Meeting>> {
    class ApiContext(var cookie: Int? = null)

    override fun handleRequest(input: ApiContext?, context: Context?): List<Meeting> {
        return getAllEntries(Table.MEETING)
    }
}