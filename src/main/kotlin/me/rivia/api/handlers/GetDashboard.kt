package me.rivia.api.handlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import me.rivia.api.database.Table
import me.rivia.api.database.getAllEntries
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import me.rivia.api.database.Meeting as BackendMeeting

class GetDashboard {
    class ApiContext(var cookie: Int? = null)

    fun handle(input: ApiContext?, context: Context?): List<BackendMeeting> {
        return getAllEntries<BackendMeeting>(Table.MEETING)
    }
}