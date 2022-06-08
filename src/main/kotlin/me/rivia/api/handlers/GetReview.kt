package me.rivia.api.handlers

import com.amazonaws.services.lambda.runtime.Context
import me.rivia.api.database.*

class GetReview {
    // get data to display the review
    companion object {
        class ApiContext(var meeting_id: Uid?, var cookie: String?) {
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
        // Fetching the necessary data
        val meetingEntry = getEntry(
            "Meeting",
            "MeetingID",
            input?.meeting_id ?: throw Error("Meeting id not present")
        ) ?: return null
        val participantEntries = getEntries(
            "User",
            "UserID",
            meetingEntry["participants"]?.ss()
                ?: throw FieldError("Meeting", "participants")
        )
        val organization = getEntry(
            "Organisation",
            "OrganisationID",
            meetingEntry["organisation"]?.s()
                ?: throw FieldError("Meeting", "organisation")
        ) ?: throw Error("OrganisationID not present")
        val presetQEntries = getEntries(
            "PresetQs",
            "PresetQID",
            organization["presetQs"]?.ss() ?: throw FieldError("Organisation", "presetQs")
        )
        return HttpResponse(getMeeting(meetingEntry),
            participantEntries.asSequence()
                .map { participantEntry -> getParticipant(participantEntry) }.toList()
                .toTypedArray(),
            presetQEntries.asSequence().map { presetQEntry -> getPresetQ(presetQEntry) }.toList()
                .toTypedArray()
        )
    }
}
