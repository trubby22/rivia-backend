package me.rivia.api.handlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import me.rivia.api.database.Table
import me.rivia.api.database.getAllEntries
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.TableSchema

class GetDashboard {
    class ApiContext(var cookie: Int? = null)

    fun handle(input: ApiContext?, context: Context?): List<Meeting> {
        return getAllEntries(Table.MEETING)
    }
}