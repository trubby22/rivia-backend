package me.rivia.api.handlers

import com.amazonaws.services.lambda.runtime.Context
import me.rivia.api.database.Table
import me.rivia.api.database.entriesNullCheck
import me.rivia.api.database.Meeting as DbMeeting
import me.rivia.api.database.getAllEntries

class GetDashboard : HandlerInit() {
    companion object {
        class ApiContext(var session: Uid?) {
            constructor() : this(null)
        }

        class IdMeeting(val meeting_id: Uid?, val meeting: Meeting?)

        class HttpResponse(val meetings: List<IdMeeting>?)
    }

    fun handle(input: ApiContext?, context: Context?): HttpResponse {
        val inputMeetings =
            entriesNullCheck(
                getAllEntries<DbMeeting>(Table.MEETING),
                Table.MEETING
            )
        val meetings: List<IdMeeting> = inputMeetings.map {
            IdMeeting(
                meeting_id = it.meetingId,
                meeting = Meeting(
                    title = it.title,
                    start_time = it.startTime,
                    end_time = it.endTime,
                )
            )
        }
        return HttpResponse(meetings)
    }
}
