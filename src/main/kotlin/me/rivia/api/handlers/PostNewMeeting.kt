package me.rivia.api.handlers

import com.amazonaws.services.lambda.runtime.Context
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import java.util.function.Consumer


class PostNewMeeting {
    companion object {
        class ApiContext(var cookie: Int?, var data: MeetingData?) {
            constructor() : this(null, null)
        }

        class MeetingData(var meeting: Meeting?, var participants: ArrayList<Uid>?) {
            constructor() : this(null, null)
        }
    }

    fun handle(input: ApiContext?, context: Context?) {
        TODO("Stuff")
    }
}
