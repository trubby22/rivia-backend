package me.rivia.api.handlers

import com.amazonaws.services.lambda.runtime.Context

class GetDashboard {
    companion object {
        class ApiContext(var session: String?) {
            constructor() : this(null)
        }

        class IdMeeting(var meeting_id: Uid?, var meeting: Meeting?)

        class HttpResponse(meetings: List<IdMeeting>?)
    }

    fun handle(input: ApiContext?, context: Context?): HttpResponse {
        val inputMeetings: List<BackendMeeting> =
            getAllEntries<BackendMeeting>(Table.MEETING)
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
