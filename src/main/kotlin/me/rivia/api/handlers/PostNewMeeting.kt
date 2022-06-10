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
                title = meeting?.title ?: throw Error("field empty"),
                startTime = meeting?.start_time ?: throw Error("field empty"),
                endTime = meeting?.end_time ?: throw Error("field empty"),
                participants = meetingData?.participants?: throw Error("field empty"),
                reviews = listOf(),
                reviewedBy = listOf()
            )
        } while(!putEntry(Table.MEETING, dbMeeting))
    }
}
