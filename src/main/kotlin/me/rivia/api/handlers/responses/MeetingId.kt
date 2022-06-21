package me.rivia.api.handlers.responses

import me.rivia.api.database.Database
import me.rivia.api.database.Table
import me.rivia.api.database.entry.Meeting as DatabaseMeeting
import me.rivia.api.database.entry.ResponseDataUser
import me.rivia.api.database.entry.ResponsePresetQ
import me.rivia.api.database.entry.PresetQ as DatabasePresetQ
import me.rivia.api.database.getEntry
import me.rivia.api.userstore.UserStore

class MeetingId(val id: String, val meeting: Meeting) {
    companion object {
        fun fetch(
            database: Database,
            userStore: UserStore,
            meetingId: String
        ): Pair<DatabaseMeeting, MeetingId>? {
            val meetingEntry = database.getEntry<DatabaseMeeting>(Table.MEETINGS, meetingId)
                ?: return null
            return meetingEntry to MeetingId(
                meetingId, Meeting(
                    meetingEntry.title!!,
                    meetingEntry.startTime!!,
                    meetingEntry.endTime!!,
                    meetingEntry.qualities!!,
                    meetingEntry.responsesCount!!,
                    meetingEntry.organizerId!!,
                    meetingEntry.userIds!!.map {
                        val userEntry = userStore.getUser(meetingEntry.tenantId!!, it)
                            ?: throw Error("User not a part of this meeting")
                        val responseDataUserEntry = database.getEntry<ResponseDataUser>(
                            Table.RESPONSEDATAUSERS,
                            ResponseDataUser.constructKey(meetingEntry.tenantId!!, it, meetingId)
                        ) ?: throw Error("No ResponseDataUserEntry")
                        UserData(
                            User(userEntry),
                            responseDataUserEntry.needed!!,
                            responseDataUserEntry.notNeeded!!,
                            responseDataUserEntry.prepared!!,
                            responseDataUserEntry.notPrepared!!
                        )
                    },
                    meetingEntry.presetQIds!!.map {
                        val presetQEntry = database.getEntry<DatabasePresetQ>(
                            Table.PRESETQS, it
                        ) ?: throw Error("PresetQ not present")
                        val responsePresetQEntry = database.getEntry<ResponsePresetQ>(
                            Table.RESPONSEPRESETQS, ResponsePresetQ(
                                presetQEntry.presetQId!!, meetingId, null, null
                            ).presetQIdMeetingId!!
                        ) ?: throw Error("responsePresetQ not present")
                        PresetQData(
                            PresetQ(presetQEntry),
                            responsePresetQEntry.numSubmitted!!,
                            responsePresetQEntry.numSelected!!
                        )
                    },
                    meetingEntry.feedbacks!!
                )
            )
        }
    }
}
