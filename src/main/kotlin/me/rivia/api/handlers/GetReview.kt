package me.rivia.api.handlers

import com.amazonaws.services.lambda.runtime.Context
import me.rivia.api.database.*
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey

class GetReview {
    // get data to display the review
    companion object {
        data class ApiContext(var meeting_id: Uid?, var cookie: String?) {
            constructor() : this(null, null)
        }

        class HttpResponse(
            val meeting: Meeting?,
            val participants: Array<Participant>?,
            val preset_qs: Array<PresetQuestion>?
        ) {
            val response_type: Int? = 1
        }

        @DynamoDbBean
        class Session(@get:DynamoDbPartitionKey var cookie: String? = null, var user: String? = null) : DbEntry {
            override fun primaryKeyName(): String = "cookie"

        }
    }

    fun handle(input: ApiContext?, context: Context?): HttpResponse? {
//        val meetingEntry = getEntry(
//            "Meeting",
//            "MeetingID",
//            input?.meeting_id ?: throw Error("Meeting id not present")
//        ) ?: return null
//        val participantEntries = getEntries(
//            "User",
//            "UserID",
//            meetingEntry["participants"]?.ss()
//                ?: throw FieldError("Meeting", "participants")
//        )
//        val organization = getEntry(
//            "Organisation",
//            "OrganisationID",
//            meetingEntry["organisation"]?.s()
//                ?: throw FieldError("Meeting", "organisation")
//        ) ?: throw Error("OrganisationID not present")
//        val presetQEntries = getEntries(
//            "PresetQs",
//            "PresetQID",
//            organization["presetQs"]?.ss() ?: throw FieldError("Organisation", "presetQs")
//        )
//        return HttpResponse(getMeeting(meetingEntry),
//            participantEntries.asSequence()
//                .map { participantEntry -> getParticipant(participantEntry) }.toList()
//                .toTypedArray(),
//            presetQEntries.asSequence().map { presetQEntry -> getPresetQ(presetQEntry) }.toList()
//                .toTypedArray()
//        )
        putEntry("Session", Session("0", "1"))
        return null
    }
}
