package me.rivia.api.handlers

import com.amazonaws.services.lambda.runtime.Context

class GetDashboard {
    companion object {
        class ApiContext(var cookie: Int?) {
            constructor() : this(null)
        }

        class IdMeeting(var meeting_id: Uid?, var meeting: Meeting?)

        class HttpResponse(meetings: ArrayList<IdMeeting>?) {
            val response_type: Int? = 4
        }
    }

    fun handle(input: ApiContext?, context: Context?): HttpResponse {
        TODO("Fill in with database fetch")
    }
}
