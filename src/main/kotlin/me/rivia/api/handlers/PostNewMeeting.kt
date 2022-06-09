package me.rivia.api.handlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import me.rivia.api.database.Table
import me.rivia.api.database.putEntry

import  me.rivia.api.database.Meeting as DatabaseMeeting

class PostNewMeeting : RequestHandler<PostNewMeeting.Companion.ApiContext?,
        Unit> {
    companion object {
        class ApiContext(var session: Int?, var data: MeetingData?) {
            constructor() : this(null, null)
        }

        class MeetingData(var meeting: Meeting?, var participants: List<Uid>?) {
            constructor() : this(null, null)
        }
    }

    override fun handleRequest(input: ApiContext?, context: Context?) {
        val meetingData: MeetingData? = input?.data
        val meeting: Meeting? = meetingData?.meeting
        val databaseMeeting: DatabaseMeeting = DatabaseMeeting(
            meetingId = generateMeetingId(),
            title = meeting?.title,
            startTime = meeting?.start_time,
            endTime = meeting?.end_time,
            participants = meetingData?.participants?.toSet(),
        )
        putEntry(Table.MEETING, databaseMeeting)
    }

    private fun generateMeetingId(): String {
        TODO("Not yet implemented")
    }
}
