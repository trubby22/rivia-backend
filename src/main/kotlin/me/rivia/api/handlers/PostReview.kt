package me.rivia.api.handlers

import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import me.rivia.api.database.Table
import me.rivia.api.database.putEntry
import me.rivia.api.database.Review as DatabaseReview

class PostReview {
    companion object {
        class ApiContext(var meeting_id: Uid?, var session: Int?, var data: Review?) {
            constructor() : this(null, null, null)
        }
    }

    fun handle(input: ApiContext?, context: Context?) {
        val review: Review? = input?.data
        val participant: Participant? = review?.participant
        val outputReview: DatabaseReview = DatabaseReview(
            reviewId = generateReviewId(),
            user = participant?.participant_id,
            notNeeded = review?.not_needed?.map { it.participant_id!! }
                ?.toSet(),
            notPrepared = review?.not_prepared?.map { it.participant_id!! }
                ?.toSet(),
            presetQs = review?.preset_qs?.toSet(),
            quality = review?.quality,
        )
//        TODO: Check whether the participant-meeting pair already exists in
//         the review database
        val success: Boolean = putEntry(Table.REVIEW, outputReview)
        if (!success) {
            println("did not manage to create new item in database")
        }
    }

    private fun generateReviewId(): String {
        TODO("Not yet implemented")
    }
}
