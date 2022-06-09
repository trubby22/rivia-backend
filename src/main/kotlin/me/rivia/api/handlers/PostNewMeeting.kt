package me.rivia.api.handlers

import com.amazonaws.services.lambda.runtime.Context

class PostNewMeeting {
    companion object {
        class ApiContext(var cookie: Int?, var data: MeetingData?) {
            constructor() : this(null, null)
        }

        class MeetingData(var meeting: Meeting?, var participants: ArrayList<Uid>?) {
            constructor() : this(null, null)
        }
    }

    fun handle(input: ApiContext?, context: Context?) {
        TODO("Stuff")
    }
}
