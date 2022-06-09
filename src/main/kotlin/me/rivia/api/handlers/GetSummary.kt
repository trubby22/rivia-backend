package me.rivia.api.handlers

import com.amazonaws.services.lambda.runtime.Context
import me.rivia.api.database.*
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.BatchGetItemRequest
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.KeysAndAttributes
import java.util.LinkedList

class GetSummary {
    companion object {
        class ApiContext(var meeting_id: Uid?, var cookie: Int?) {
            constructor() : this(null, null)
        }

        class HttpResponse(var meeting: Meeting?, var responses: Array<Response>?) {
            val response_type: Int? = 2
        }
    }
    // ignore cookie
    fun handle(input: ApiContext?, context: Context?): HttpResponse {

        val meeting_id2: String = input?.meeting_id!!;
        val mapMeetingTable = getEntry("Meeting", "MeetingID", meeting_id2);

        return HttpResponse(null, null)
    }
}
