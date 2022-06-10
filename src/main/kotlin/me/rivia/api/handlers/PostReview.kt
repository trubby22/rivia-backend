package me.rivia.api.handlers

import com.amazonaws.services.lambda.runtime.Context
import me.rivia.api.database.*
import me.rivia.api.database.Meeting
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import me.rivia.api.database.Review as DbReview

// Meeting, Review

class PostReview : HandlerInit() {
    companion object {
        class Review(
            var quality: Float?,
            var preset_qs: List<String>?,
            var not_needed: List<String>?,
            var not_prepared: List<String>?,
            var feedback: String?
        ) {
            constructor() : this(null, null, null, null, null)
        }

        class ApiContext(var meeting_id: Uid?, var session: Uid?, var data: Review?) {
            constructor() : this(null, null, null)
        }
    }

    fun handle(input: ApiContext?, context: Context?) {
        var meetingEntry = entryNullCheck(
            getEntry<Meeting>(
                Table.MEETING, input?.meeting_id ?: throw Error("Meeting id not present")
            ) ?: return, Table.MEETING
        )

        var userId = getUser(input.session) ?: return

        if (meetingEntry.reviewedBy!!.contains(userId)) {
            return
        }

        val review = input.data ?: return
        lateinit var outputReview: DbReview
        do {
            outputReview = DbReview(
                reviewId = generateId(),
                user = userId,
                notNeeded = review.not_needed,
                notPrepared = review.not_prepared,
                presetQs = review.preset_qs ?: return,
                quality = review.quality ?: return,
                feedback = review.feedback ?: return,
            )
        } while (!putEntry(Table.REVIEW, outputReview))

        do {
            meetingEntry = entryNullCheck(
                getEntry<Meeting>(
                    Table.MEETING, input.meeting_id!!
                ) ?: return, Table.MEETING
            )
            val oldReviewedBy = meetingEntry.reviewedBy!!
            meetingEntry.reviewedBy = meetingEntry.reviewedBy!! + listOf(userId)
        } while (!updateEntry(
                Table.MEETING,
                meetingEntry,
                "reviewedBy",
                AttributeValue.fromL(oldReviewedBy.map { AttributeValue.fromS(it) })
            )
        )
    }
}
