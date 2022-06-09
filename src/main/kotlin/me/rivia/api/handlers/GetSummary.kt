package me.rivia.api.handlers

import com.amazonaws.services.lambda.runtime.Context
import me.rivia.api.database.getEntry
import me.rivia.api.database.getEntries
import me.rivia.api.database.FieldError
import me.rivia.api.database.Table
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
        val meetingEntry = getEntry<DbMeeting>(
            Table.MEETING,
            input?.meeting_id ?: throw Error("Meeting id not present")
        ) ?: return null
        val reviewEntries = getEntries<DbReview>(
            Table.REVIEW,
            meetingEntry.reviews?.asIterable() ?: throw FieldError(Table.MEETING, "reviews")
        )
        if (reviewEntries.size != meetingEntry.participants?.size) {
            throw Error("some reviewIds not present")
        }

        val allPresetQIds = reviewEntries.asSequence().map { reviewEntry ->
            reviewEntry.presetQs?.asSequence() ?: throw FieldError(Table.REVIEW, "presetQs")
        }.flatten().toSet()
        val presetQIds = getEntries<DbPresetQ>(Table.PRESETQS, allPresetQIds).asSequence()
            .map { presetQEntry ->
                (presetQEntry.presetQId ?: throw FieldError(
                    Table.PRESETQS,
                    "presetQId"
                )) to (presetQEntry.text ?: throw FieldError(Table.PRESETQS, "text"))
            }.toMap()

        val allParticipantsIds = reviewEntries.asSequence().map { reviewEntry ->
            sequenceOf(reviewEntry.user ?: throw FieldError(Table.REVIEW, "user")) +
                    (reviewEntry.notNeeded?.asSequence() ?: throw FieldError(
                        Table.REVIEW,
                        "notNeeded"
                    )) +
                    (reviewEntry.notPrepared?.asSequence() ?: throw FieldError(
                        Table.REVIEW,
                        "notPrepared"
                    ))
        }.flatten().toSet()
        val participantIds = getEntries<DbUser>(
            Table.USER,
            allParticipantsIds
        ).map { participantEntry -> Participant(participantEntry.userId ?: throw FieldError(Table.REVIEW), ) }

        return HttpResponse(
            Meeting(
                meetingEntry.title ?: throw FieldError(Table.MEETING, "title"),
                meetingEntry.startTime ?: throw FieldError(Table.MEETING, "startTime"),
                meetingEntry.endTime ?: throw FieldError(Table.MEETING, "endTime")
            ),
            reviewEntries.map { reviewEntry ->
                Review(
                    reviewEntry.user ?: throw FieldError(Table.REVIEW, "user"),
                    reviewEntry.quality ?: throw FieldError(Table.REVIEW, "quality"),
                    reviewEntry.reviewId ?: throw FieldError(Table.REVIEW, "reviewId"),
                    (reviewEntry.presetQs ?: throw FieldError(
                        Table.REVIEW,
                        "presetQs"
                    )).asSequence(),
                    null,
                    null,
                )
            }.toTypedArray()
        )
    }
}
