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

        val meeting_id2 : String = input?.meeting_id!!;
        val mapMeetingTable = getEntry("Meeting", "MeetingID", meeting_id2);
//        val idLists : AttributeValue? = mapMeetingTable?.get("responses")
//        val listMapReviewTable = getEntries("Review", "ReviewID", idLists?.ns()!!)





        return HttpResponse(null, null)
    }
    // takes mapping from database anc converts into parsed mapping of return type
    fun getMeeting(map : Map<String, AttributeValue>) : Meeting {
        return Meeting(
            map["title"]!!.s(),
            // check whether start time and end time are string or int
            map["startTime"]!!.n().toInt(),
            map["endTime"]!!.n().toInt()
        )
    }
    // takes mapping from database anc converts into parsed mapping of return type
//    fun getResponse(map : Map<String, AttributeValue>) : Response {
//        return Response(
//            getParticipant(getEntry("User", "UserID", map["UserID"]!!.s())),
//            // check whether start time and end time are string or int
//            map["quality"]!!.n().toInt(),
//            map["preset_qs"]!!.n().toInt(),
//            map["not_needed"]!!.n().toInt(),
//            map["not_prepared"]!!.n().toInt(),
//        )
//    }
    // takes mapping from database anc converts into parsed mapping of return type
    fun getParticipant(map : Map<String, AttributeValue>) : Participant {
        return Participant(
            map["UserID"]!!.s(),
            // check whether start time and end time are string or int
            map["name"]!!.s(),
            map["surname"]!!.s(),
            map["email"]!!.s(),
        )
    }
}
