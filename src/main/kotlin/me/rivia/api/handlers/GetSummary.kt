package me.rivia.api.handlers

import com.amazonaws.services.lambda.runtime.Context
import me.rivia.api.database.getEntry
import me.rivia.api.database.getEntries
import me.rivia.api.database.getAllEntries
import me.rivia.api.database.FieldError
import me.rivia.api.database.Table
import me.rivia.api.database.PresetQ as DbPresetQ
import me.rivia.api.database.Meeting as DbMeeting
import me.rivia.api.database.Review as DbReview

class GetSummary {
    companion object {
        class ApiContext(var meeting_id: Uid?, var session: String?) {
            constructor() : this(null, null)
        }

        class HttpResponse(var meeting: Meeting?, var responses: Array<Response>?)
    }

    fun handle(input: ApiContext?, context: Context?): HttpResponse? {
        val meetingEntry = getEntry<DbMeeting>(
            Table.MEETING,
            input?.meeting_id ?: throw Error("Meeting id not present")
        ) ?: return null
        val reviewEntries = getEntries<DbReview>(
            Table.USER,
            meetingEntry.reviews?.asIterable() ?: throw FieldError("Meeting", "reviews")
        )
        if (reviewEntries.size != meetingEntry.participants?.size) {
            throw Error("some reviewIds not present")
        }
        
        return HttpResponse(null, null)
    }
}
