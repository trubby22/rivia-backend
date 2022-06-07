package me.rivia.api.handlers

import com.amazonaws.services.lambda.runtime.Context

class GetNewMeeting {
    companion object {
        class ApiContext(val cookie: Int?) {
            constructor() : this(null)
        }

        class HttpResponse(val meetings: Array<Participant>) {
            val response_type = 3
        }
    }

    fun handle(input: ApiContext, context: Context): HttpResponse {
        TODO("Fill in with database fetch")
    }
}
