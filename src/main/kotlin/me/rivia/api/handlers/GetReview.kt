package me.rivia.api.handlers
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import main.*
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest
import software.amazon.awssdk.regions.Region.EU_WEST_2
import software.amazon.awssdk.services.dynamodb.DynamoDbClient

class GetReview {
    // get data to display the review
    companion object {
        data class ApiContext(val meeting_id: Uid?, val cookie: Int?) {
            constructor() : this(null, null)
        }

        class HttpResponse(
            val meeting: Meeting?,
            val participants: Array<Participant>?,
            val preset_qs: Array<PresetQuestion>?
        ) {
            val response_type: Int? = 1
        }
    }

    fun handle(input: ApiContext?, context: Context?): HttpResponse {
        val ddb = DynamoDbClient.builder()
            .region(EU_WEST_2)
            .build()

        val enhancedClient = DynamoDbEnhancedClient.builder()
            .dynamoDbClient(ddb)
            .build()

        // allows us to map the class with the fields stored in the table
        // important differentiaton between meeting and main.meeting
        val mappedTable: DynamoDbTable<main.Meeting> =
            enhancedClient.table("Meeting", TableSchema.fromBean(main.Meeting::class.java))

        val meetingIDKey: Key = Key.builder()
            .partitionValue("0")
            .build()
        mappedTable.scan().toString();
//        val result: main.Meeting? = mappedTable.getItem { r: GetItemEnhancedRequest.Builder -> r.key(meetingIDKey)
//        }
//
//        val mappedTable2: DynamoDbTable<Participant>
//                = enhancedClient.table("Participant", TableSchema.fromBean(Participant::class.java))
//
//
//        // get participant list info by going through users and finding their corresponding info in table USER
//
//        val participantList : MutableList<Participant> = mutableListOf()
//        // response type is 0 for the moment
//        for (user_id in result!!.participants!!) {
//            val userIDKey: Key = Key.builder()
//                .partitionValue(user_id)
//                .build()
//            val participantResult: Participant? = mappedTable2.getItem { r: GetItemEnhancedRequest.Builder -> r.key(
//                userIDKey
//            )
//            }
//            if (participantResult != null) {
//                participantList.add(participantResult)
//            } else {
//                println("UserID associated with user in meeting not found")
//            }
//        }
//        // get question list (don't have seperate table at the moment)
//        val qList : MutableList<PresetQuestion> = mutableListOf()
//        var i = 0;
//        // asssigns 1,2,3,4... to list of questions
//        for (preset_q_text in result!!.preset_qs!!) {
//            qList.add(PresetQuestion(i.toString(), preset_q_text))
//            i++
//        }
        return HttpResponse(null, null, null)
//        return HttpResponse(
//            Meeting(result!!.title, result!!.startTime, result!!.endTime),
//            participantList.toTypedArray(), qList.toTypedArray()
//        )
//        return HttpResponse(
//            Meeting("Meeting", 0, 1),
//            arrayOf(Participant("0000-0000-0000-0000", "John", "Doe", "example@gmail.com")),
//            arrayOf(
//                MeetingPainPoint("0000-0000-0000-0000", "Example pain preset_q")
//            )
//        )
    }
}
