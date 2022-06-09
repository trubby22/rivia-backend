package me.rivia.api.handlers

import com.amazonaws.services.lambda.runtime.Context
import me.rivia.api.database.*
import me.rivia.api.database.PresetQ as DbPresetQ
import me.rivia.api.database.Meeting as DbMeeting
import me.rivia.api.database.User as DbUser

class GetReview {
    companion object {
        data class ApiContext(var meeting_id: Uid?, var session: String?) {
            constructor() : this(null, null)
        }

        class HttpResponse(
            val meeting: Meeting?,
            val participants: List<Participant>?,
            val preset_qs: List<PresetQuestion>?
        )
    }

    fun handle(input: ApiContext?, context: Context?): HttpResponse? {
        val meetingEntry = getEntry<DbMeeting>(
            Table.MEETING,
            input?.meeting_id ?: throw Error("Meeting id not present")
        ) ?: return null

        if (meetingEntry.reviewedBy?.contains(
                getUser(input.session) ?: return null
            ) ?: throw FieldError(Table.MEETING, "reviewedBy")
        ) {
            return null
        }

        val participantEntries = getEntries<DbUser>(
            Table.USER,
            meetingEntry.participants ?: throw FieldError(
                Table.MEETING,
                "participants"
            )
        )
        if (participantEntries.size != meetingEntry.participants?.size) {
            throw Error("some userIds not present")
        }

        val presetQEntries = getAllEntries<DbPresetQ>(
            Table.PRESETQS,
        )
        return HttpResponse(
            Meeting(
                meetingEntry.title ?: throw FieldError(Table.MEETING, "title"),
                meetingEntry.startTime ?: throw FieldError(Table.MEETING, "startTime"),
                meetingEntry.endTime ?: throw FieldError(Table.MEETING, "endTime")
            ),
            participantEntries
                .map { participantEntry ->
                    Participant(
                        participantEntry.userId ?: throw FieldError(Table.USER, "userId"),
                        participantEntry.name ?: throw FieldError(Table.USER, "name"),
                        participantEntry.surname ?: throw FieldError(Table.USER, "surname"),
                        participantEntry.email ?: throw FieldError(Table.USER, "email"),
                    )
                },
            presetQEntries
                .map { presetQEntry ->
                    PresetQuestion(
                        presetQEntry.presetQId ?: throw FieldError(
                            Table.PRESETQS,
                            "presetQId"
                        ), presetQEntry.text ?: throw FieldError(Table.PRESETQS, "text")
                    )
                }
        )
    }
}
