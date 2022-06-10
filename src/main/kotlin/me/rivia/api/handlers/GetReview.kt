package me.rivia.api.handlers

import com.amazonaws.services.lambda.runtime.Context
import me.rivia.api.database.*
import me.rivia.api.database.PresetQ as DbPresetQ
import me.rivia.api.database.Meeting as DbMeeting
import me.rivia.api.database.User as DbUser

class GetReview : HandlerInit() {
    companion object {
        data class ApiContext(var meeting_id: Uid?, var session: Uid?) {
            constructor() : this(null, null)
        }

        class HttpResponse(
            val meeting: Meeting?,
            val participants: List<Participant>?,
            val preset_qs: List<PresetQuestion>?
        )
    }

    fun handle(input: ApiContext?, context: Context?): HttpResponse? {
        val meetingEntry = entryNullCheck(
            getEntry<DbMeeting>(
                Table.MEETING,
                input?.meeting_id ?: throw Error("Meeting id not present")
            ) ?: return null, Table.MEETING
        )

        if (meetingEntry.reviewedBy!!.contains(
                getUser(input.session) ?: return null
            )
        ) {
            return null
        }

        val participantEntries = entriesNullCheck(
            getEntries<DbUser>(
                Table.USER,
                meetingEntry.participants!!
            ), Table.USER
        )
        if (participantEntries.size != meetingEntry.participants?.size) {
            throw Error("some userIds not present")
        }

        val presetQEntries = entriesNullCheck(
            getAllEntries<DbPresetQ>(
                Table.PRESETQS,
            ), Table.PRESETQS
        )
        return HttpResponse(
            Meeting(
                meetingEntry.title,
                meetingEntry.startTime,
                meetingEntry.endTime
            ),
            participantEntries
                .map { participantEntry ->
                    Participant(
                        participantEntry.userId,
                        participantEntry.name,
                        participantEntry.surname,
                        participantEntry.email,
                    )
                },
            presetQEntries
                .map { presetQEntry ->
                    PresetQuestion(
                        presetQEntry.presetQId,
                        presetQEntry.text,
                    )
                }
        )
    }
}
