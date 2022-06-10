package me.rivia.api.handlers

import com.amazonaws.services.lambda.runtime.Context
import me.rivia.api.database.Table
import me.rivia.api.database.generateId
import me.rivia.api.database.putEntry

import  me.rivia.api.database.Meeting as DbMeeting

// Meeting

class PostNewMeeting : HandlerInit() {
    companion object {
        class ApiContext(var session: Uid?, var data: MeetingData?) {
            constructor() : this(null, null)
        }

        class MeetingData(var meeting: Meeting?, var participants: List<Uid>?) {
            constructor() : this(null, null)
        }
    }

    fun handle(input: ApiContext?, context: Context?) {
        val meetingData: MeetingData? = input?.data
        val meeting: Meeting? = meetingData?.meeting
        lateinit var dbMeeting: DbMeeting
        do {
            dbMeeting = DbMeeting(
                meetingId = generateId(),
                title = meeting?.title,
                startTime = meeting?.start_time,
                endTime = meeting?.end_time,
                participants = meetingData?.participants,
            )
        } while(!putEntry(Table.MEETING, dbMeeting))
    }
}
