package me.rivia.api.handlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import me.rivia.api.database.Table
import me.rivia.api.database.generateId
import me.rivia.api.database.putEntry

import  me.rivia.api.database.Meeting as DbMeeting

class PostNewMeeting : HandlerInit() {
    companion object {
        class ApiContext(var session: Int?, var data: MeetingData?) {
            constructor() : this(null, null)
        }

        class MeetingData(var meeting: Meeting?, var participants: List<Uid>?) {
            constructor() : this(null, null)
        }
    }

    fun handle(input: ApiContext?, context: Context?) {
        val meetingData: MeetingData? = input?.data
        val meeting: Meeting? = meetingData?.meeting
        val databaseMeeting: DbMeeting = DbMeeting(
            meetingId = generateId(),
            title = meeting?.title,
            startTime = meeting?.start_time,
            endTime = meeting?.end_time,
            participants = meetingData?.participants,
        )
        putEntry(Table.MEETING, databaseMeeting) // check if the uid was ok; if not then remake it
    }
}
