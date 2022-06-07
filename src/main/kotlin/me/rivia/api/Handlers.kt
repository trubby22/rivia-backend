package me.rivia.api

import com.amazonaws.services.lambda.runtime.Context

class GetReviewHandler {
    companion object {
        data class ApiContext(val meeting_id: String)
        class Response(val response_type: Int, val meeting: Meeting, val participants: Array<Participant>, val points: Array<MeetingPainPoint>)
    }
    fun handle(input: ApiContext, context: Context): Response {
        TODO("Fill in with database fetch")
    }
}
