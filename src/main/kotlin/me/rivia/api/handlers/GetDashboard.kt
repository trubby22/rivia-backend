package me.rivia.api.handlers

import com.amazonaws.services.lambda.runtime.Context
import me.rivia.api.database.Table
import me.rivia.api.database.Meeting as DbMeeting
import me.rivia.api.database.getAllEntries

class GetDashboard : HandlerInit() {
    companion object {
        class ApiContext(var session: String?) {
            constructor() : this(null)
        }

        class IdMeeting(var meeting_id: Uid?, var meeting: Meeting?)

        class HttpResponse(meetings: List<IdMeeting>?)
    }

    fun handle(input: ApiContext?, context: Context?): HttpResponse {
        val inputMeetings =
            getAllEntries<DbMeeting>(Table.MEETING) // wrap in null check
        E
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
