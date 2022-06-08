package me.rivia.api.handlers

import com.amazonaws.services.lambda.runtime.Context

class GetSummary {
    companion object {
        class ApiContext(var meeting_id: Uid?, var cookie: Int?) {
            constructor() : this(null, null)
        }

        class HttpResponse(var meeting: Meeting?, var responses: Array<Response>?) {
            val response_type: Int? = 2
        }
    }

    fun handle(input: ApiContext?, context: Context?): HttpResponse {
        TODO("Fill in with database fetch")
    }
}
