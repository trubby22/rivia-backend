package me.rivia.api.handlers

import com.amazonaws.services.lambda.runtime.Context
import me.rivia.api.database.getEntry

class GetReview {
    // get data to display the review
    companion object {
        class ApiContext(var meeting_id: Uid?, var cookie: String?) {
            constructor() : this(null, null)
        }

        class HttpResponse(
            val meeting: Meeting?,
            val participants: Array<Participant>?,
            val preset_qs: Array<PresetQuestion>?
        ) {
            val response_type: Int? = 1
        }
    }

    fun handle(input: ApiContext?, context: Context?): HttpResponse? {
        val meetingEntry = getEntry("Meeting", "MeetingID",  input?.meeting_id ?: throw Error("Meeting id not present")) ?: return null
//        return meetingEntry.toString()
        throw Error("Succeeded")
    }
}
