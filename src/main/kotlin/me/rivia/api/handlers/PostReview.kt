package me.rivia.api.handlers

import com.amazonaws.services.lambda.runtime.Context

class PostReview {
    companion object {
        class ApiContext(val meeting_id: Uid?, val cookie: Int?, val data: Review?) {
            constructor() : this(null, null, null)
        }

        class Review(
            val quality: Float?,
            val points: Array<Uid>?,
            val not_needed: Array<Uid>?,
            val not_prepared: Array<Uid>?,
            feedback: String?
        ) {
            constructor() : this(null, null, null, null, null)
        }
    }

    fun handle(input: ApiContext?, context: Context?) {
        TODO("Fill in with database fetch")
    }
}
