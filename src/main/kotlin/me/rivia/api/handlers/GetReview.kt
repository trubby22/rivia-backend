package me.rivia.api.handlers

import com.amazonaws.services.lambda.runtime.Context
import me.rivia.api.database.getEntry
import me.rivia.api.database.getEntries
import me.rivia.api.database.getAllEntries
import me.rivia.api.database.FieldError
import me.rivia.api.database.PresetQ as DbPresetQ
import me.rivia.api.database.Meeting as DbMeeting
import me.rivia.api.database.User as DbUser

class GetReview {
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
    }

    fun handle(input: ApiContext?, context: Context?): HttpResponse? {
        val meetingEntry = getEntry<DbMeeting>(
            "Meeting",
            input?.meeting_id ?: throw Error("Meeting id not present")
        ) ?: return null

        val participantEntries = getEntries<DbUser>(
            "User",
            meetingEntry.participants?.asIterable() ?: throw FieldError("Meeting", "participants")
        )
        if (participantEntries.size != meetingEntry.participants?.size) {
            throw Error("some userIds not present")
        }

        val presetQEntries = getAllEntries<DbPresetQ>(
            "PresetQs",
        )
        return HttpResponse(
            Meeting(meetingEntry.title, meetingEntry.startTime, meetingEntry.endTime),
            participantEntries.asSequence()
                .map { participantEntry ->
                    Participant(
                        participantEntry.userId,
                        participantEntry.name,
                        participantEntry.surname,
                        participantEntry.email
                    )
                }.toList()
                .toTypedArray(),
            presetQEntries.asSequence()
                .map { presetQEntry -> PresetQuestion(presetQEntry.presetQId, presetQEntry.text) }
                .toList()
                .toTypedArray()
        )
    }
}
