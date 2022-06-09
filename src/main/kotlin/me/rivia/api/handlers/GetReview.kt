package me.rivia.api.handlers

import com.amazonaws.services.lambda.runtime.Context
import me.rivia.api.database.*
import java.lang.reflect.Field
import me.rivia.api.database.PresetQ as DbPresetQ
import me.rivia.api.database.Meeting as DbMeeting
import me.rivia.api.database.User as DbUser
import me.rivia.api.database.Session as DbSession

class GetReview {
    companion object {
        data class ApiContext(var meeting_id: Uid?, var session: String?) {
            constructor() : this(null, null)
        }

        class HttpResponse(
            val meeting: Meeting?,
            val participants: Array<Participant>?,
            val preset_qs: Array<PresetQuestion>?
        )
    }

    fun handle(input: ApiContext?, context: Context?): HttpResponse? {
        val meetingEntry = getEntry<DbMeeting>(
            Table.MEETING,
            input?.meeting_id ?: throw Error("Meeting id not present")
        ) ?: return null

        if (meetingEntry.reviewedBy?.contains(
                getUser(input.session)?: return null
            ) ?: throw FieldError(Table.MEETING, "reviewedBy")
        ) {
            return null
        }

        val participantEntries = getEntries<DbUser>(
            Table.USER,
            meetingEntry.participants?.asIterable() ?: throw FieldError(Table.MEETING, "participants")
        )
        if (participantEntries.size != meetingEntry.participants?.size) {
            throw Error("some userIds not present")
        }

        val presetQEntries = getAllEntries<DbPresetQ>(
            Table.PRESETQS,
        )
        return HttpResponse(
            Meeting(
                meetingEntry.title,
                meetingEntry.startTime,
                meetingEntry.endTime
            ),
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
                .map { presetQEntry ->
                    PresetQuestion(
                        presetQEntry.presetQId,
                        presetQEntry.text
                    )
                }
                .toList()
                .toTypedArray()
        )
    }
}
