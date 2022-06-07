package me.rivia.api.handlers

import com.amazonaws.services.lambda.runtime.Context

class GetSummary {
    companion object {
        class ApiContext(val meeting_id: Uid?, val cookie: Int?) {
            constructor() : this(null, null)
        }

        class HttpResponse(val meeting: Meeting?, val responses: Array<Response>?) {
            val response_type = 2
        }
    }

    fun handle(input: ApiContext?, context: Context?): HttpResponse {
        TODO("Fill in with database fetch")
    }
}
