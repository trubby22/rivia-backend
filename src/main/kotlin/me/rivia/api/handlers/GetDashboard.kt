package me.rivia.api.handlers

import com.amazonaws.services.lambda.runtime.Context

class GetDashboard {
    companion object {
        class ApiContext(val cookie: Int?) {
            constructor() : this(null)
        }

        class IdMeeting(val meeting_id: Uid?, val meeting: Meeting?)

        class HttpResponse(meetings: Array<IdMeeting>?) {
            val response_type: Int? = 4
        }
    }

    fun handle(input: ApiContext?, context: Context?): HttpResponse {
        TODO("Fill in with database fetch")
    }
}
