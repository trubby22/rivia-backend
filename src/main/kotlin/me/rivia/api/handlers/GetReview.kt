package me.rivia.api.handlers

import com.amazonaws.services.lambda.runtime.Context
import main.MeetingDB
import me.rivia.api.database.enhancedClient
import me.rivia.api.database.getEntry
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest

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

    fun handle(input: ApiContext?, context: Context?): HttpResponse? {
        // allows us to map the class with the fields stored in the table
        val meetingTable: DynamoDbTable<MeetingDB> =
            enhancedClient.table("Meeting", TableSchema.fromBean(MeetingDB::class.java))
        val meetingEntry = getEntry<MeetingDB>("Meeting", input?.meeting_id ?: return null)

        val participantTable: DynamoDbTable<Participant> =
            enhancedClient.table("Participant", TableSchema.fromBean(Participant::class.java))


        // get participant list info by going through users and finding their corresponding info in table USER

        val participantList: MutableList<Participant> = mutableListOf()
        // response type is 0 for the moment
        for (user_id in result!!.participants) {
            val userIDKey: Key = Key.builder().partitionValue(user_id).build()
            val participantResult: Participant? =
                participantTable.getItem { r: GetItemEnhancedRequest.Builder ->
                    r.key(
                        userIDKey
                    )
                }
            if (participantResult != null) {
                participantList.add(participantResult)
            } else {
                println("UserID associated with user in meeting not found")
            }
        }
        // get question list (don't have seperate table at the moment)
        val qList: MutableList<PresetQuestion> = mutableListOf()
        var i = 0;
        // asssigns 1,2,3,4... to list of questions
        for (preset_q_text in result!!.preset_qs) {
            qList.add(PresetQuestion(i.toString(), preset_q_text))
            i++
        }
        return HttpResponse(
            Meeting(result!!.title, result!!.startTime, result!!.endTime),
            participantList.toTypedArray(),
            qList.toTypedArray()
        )
    }
}
