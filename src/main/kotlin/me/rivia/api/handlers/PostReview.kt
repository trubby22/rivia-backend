package me.rivia.api.handlers

import com.amazonaws.services.lambda.runtime.Context
import me.rivia.api.database.Table
import me.rivia.api.database.generateId
import me.rivia.api.database.putEntry
import me.rivia.api.database.Review as DbReview

class PostReview : HandlerInit() {
    companion object {
        class ApiContext(var meeting_id: Uid?, var session: Int?, var data: Review?) {
            constructor() : this(null, null, null)
        }
    }

    fun handle(input: ApiContext?, context: Context?) {
        val review: Review? = input?.data
        val participant: Participant? = review?.participant
        val outputReview: DbReview = DbReview(
            reviewId = generateId(),
            user = participant?.participant_id,
            notNeeded = review?.not_needed?.map { it.participant_id!! },
            notPrepared = review?.not_prepared?.map { it.participant_id!! },
            presetQs = review?.preset_qs,
            quality = review?.quality,
        )
//        TODO: Check whether the participant-meeting pair already exists in
//         the review database
        val success: Boolean = putEntry(Table.REVIEW, outputReview) // while loop with a check
        if (!success) {
            println("did not manage to create new item in database")
        }
    }
}
