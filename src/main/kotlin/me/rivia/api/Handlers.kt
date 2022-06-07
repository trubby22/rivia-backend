package me.rivia.api

import com.amazonaws.services.lambda.runtime.Context

class GetReviewHandler {
    companion object {
        data class ApiContext(val meeting_id: String?, val cookie: Int?) {
            constructor() : this(null, null)
        }
        class Response(val response_type: Int, val meeting: Meeting, val participants: Array<Participant>, val points: Array<MeetingPainPoint>)
    }
    fun handle(input: ApiContext, context: Context): Response {
        TODO("Fill in with database fetch")
    }
}

class PostReviewHandler {
    companion object {
        class ApiContext(val meeting_id: String?, val cookie: Int?, val data: Review?) {
            constructor() : this(null, null, null)
        }
        class Review(val quality: Float?, val points: Array<Uid>?, val not_needed: Array<Uid>?, val not_prepared: Array<Uid>?, feedback: String?) {
            constructor() : this(null, null, null, null, null)
        }
    }
    fun handle(input: ApiContext, context: Context) {
        TODO("Fill in with database fetch")
    }
}
