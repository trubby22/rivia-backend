package me.rivia.api.handlers

import com.amazonaws.services.lambda.runtime.Context

class PostNewMeeting {
    companion object {
        class ApiContext(val cookie: Int?, val data: MeetingData?) {
            constructor() : this(null, null)
        }

        class MeetingData(val meeting: Meeting?, val participants: Array<Uid>?)
    }

    fun handle(input: ApiContext, context: Context) {
        TODO("Fill in with database fetch")
    }
}
