package me.rivia.api.handlers

import com.amazonaws.services.lambda.runtime.Context
import me.rivia.api.database.*
import me.rivia.api.database.Meeting
import me.rivia.api.database.Review as DbReview

// Meeting, Review

class PostReview : HandlerInit() {
    companion object {
        class ApiContext(var meeting_id: Uid?, var session: Uid?, var data: Review?) {
            constructor() : this(null, null, null)
        }
    }

    fun handle(input: ApiContext?, context: Context?) {
        val meetingEntry = entryNullCheck(
            getEntry<Meeting>(
            Table.MEETING,
            input?.meeting_id ?: throw Error("Meeting id not present")
        ) ?: return, Table.MEETING)

        if (meetingEntry.reviewedBy!!.contains(
                getUser(input.session) ?: return
            )
        ) {
            return
        }

        val review: Review? = input?.data
        val participant: Participant? = review?.participant
        lateinit var outputReview: DbReview
        do {
            outputReview = DbReview(
                reviewId = generateId(),
                user = participant?.participant_id,
                notNeeded = review?.not_needed?.map { it.participant_id!! },
                notPrepared = review?.not_prepared?.map { it.participant_id!! },
                presetQs = review?.preset_qs,
                quality = review?.quality,
            )
        } while (!putEntry(Table.REVIEW, outputReview))

    }
}
