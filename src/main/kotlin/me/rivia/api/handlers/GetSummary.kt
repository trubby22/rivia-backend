package me.rivia.api.handlers

import com.amazonaws.services.lambda.runtime.Context
import me.rivia.api.database.*
import me.rivia.api.database.PresetQ as DbPresetQ
import me.rivia.api.database.Meeting as DbMeeting
import me.rivia.api.database.Review as DbReview
import me.rivia.api.database.User as DbUser

class GetSummary {
    companion object {
        class ApiContext(var meeting_id: Uid?, var session: String?) {
            constructor() : this(null, null)
        }

        class HttpResponse(var meeting: Meeting?, var responses: List<Review>?)
    }

    fun handle(input: ApiContext?, context: Context?): HttpResponse? {
        val meetingEntry = entryNullCheck(
            getEntry<DbMeeting>(
                Table.MEETING,
                input?.meeting_id ?: throw Error("Meeting id not present")
            ) ?: return null, Table.MEETING
        )

        val reviewEntries = entriesNullCheck(
            getEntries<DbReview>(
                Table.REVIEW,
                meetingEntry.reviews!!
            ), Table.REVIEW
        )
        if (reviewEntries.size != meetingEntry.participants?.size) {
            throw Error("some reviewIds not present")
        }

        // All the presetQIds we're gonna need
        val allPresetQIds = reviewEntries.asSequence().map { reviewEntry ->
            reviewEntry.presetQs!!.asSequence()
        }.flatten().toSet()
        val presetQIds = entriesNullCheck(
            getEntries<DbPresetQ>(Table.PRESETQS, allPresetQIds),
            Table.PRESETQS
        ).asSequence()
            .map { presetQEntry ->
                presetQEntry.presetQId!! to presetQEntry.text!!
            }.toMap()
        if (presetQIds.size != allPresetQIds.size) {
            throw Error("some presetQIds not present")
        }

        // All the participantIds we're gonna need
        val allParticipantIds = reviewEntries.asSequence().map { reviewEntry ->
            sequenceOf(reviewEntry.user!!) +
                    reviewEntry.notNeeded!!.asSequence() +
                    reviewEntry.notPrepared!!.asSequence()
        }.flatten().toSet()
        val participantIds = entriesNullCheck(
            getEntries<DbUser>(
                Table.USER,
                allParticipantIds
            ), Table.USER
        ).map { participantEntry ->
            participantEntry.userId!! to
                    Participant(
                        participantEntry.userId!!,
                        participantEntry.name!!,
                        participantEntry.surname!!,
                        participantEntry.email!!
                    )
        }.toMap()
        if (participantIds.size != allParticipantIds.size) {
            throw Error("some presetQIds not present")
        }

        return HttpResponse(
            Meeting(
                meetingEntry.title!!,
                meetingEntry.startTime!!,
                meetingEntry.endTime!!
            ),
            reviewEntries.map { reviewEntry ->
                Review(
                    participantIds[reviewEntry.user!!]!!,
                    reviewEntry.quality!!,
                    reviewEntry.presetQs!!.map { presetQId -> presetQIds[presetQId]!! },
                    reviewEntry.notNeeded!!.map { participantId -> participantIds[participantId]!! },
                    reviewEntry.notPrepared!!.map { participantId -> participantIds[participantId]!! },
                )
            }
        )
    }
}
