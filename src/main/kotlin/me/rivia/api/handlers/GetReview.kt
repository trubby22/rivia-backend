package me.rivia.api.handlers

import com.amazonaws.services.lambda.runtime.Context

class GetReview {

    companion object {
        data class ApiContext(val meeting_id: Uid?, val cookie: Int?) {
            constructor() : this(null, null)
        }

        class HttpResponse(
            val meeting: Meeting?,
            val participants: Array<Participant>?,
            val points: Array<MeetingPainPoint>?
        ) {
            val response_type = 1
        }
    }

    fun handle(input: ApiContext?, context: Context?): HttpResponse {
        return HttpResponse(
            Meeting("Meeting", 0, 1),
            arrayOf(Participant("0000-0000-0000-0000", "John", "Doe", "example@gmail.com")),
            arrayOf(
                MeetingPainPoint("0000-0000-0000-0000", "Example pain point")
            )
        )
    }
}
