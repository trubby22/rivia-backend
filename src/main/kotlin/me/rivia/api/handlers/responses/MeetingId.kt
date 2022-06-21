package me.rivia.api.handlers.responses

import me.rivia.api.database.Database
import me.rivia.api.database.Table
import me.rivia.api.database.entry.Meeting as DatabaseMeeting
import me.rivia.api.database.entry.ResponseDataUsers
import me.rivia.api.database.entry.ResponsePresetQ
import me.rivia.api.database.getEntry

class MeetingId(val id: String, val meeting: Meeting) {
    companion object {
        fun fetch(database: Database, meetingId: String): MeetingId {
            val meetingEntry = database.getEntry<DatabaseMeeting>(Table.MEETINGS, meetingId)
                ?: throw Error("Meeting not present")
            return MeetingId(
                meetingId, Meeting(
                    meetingEntry.title!!,
                    meetingEntry.startTime!!,
                    meetingEntry.endTime!!,
                    meetingEntry.qualities!!,
                    meetingEntry.responsesCount!!,
                    meetingEntry.organizerId!!,
                    meetingEntry.participantIds!!.map {
                        val participantEntry =
                            database.getEntry<me.rivia.api.database.entry.User>(
                                Table.PARTICIPANTS, it
                            ) ?: throw Error("Participant not present")
                        val responseParticipantEntry = database.getEntry<ResponseDataUsers>(
                            Table.RESPONSEPARTICIPANTS, ResponseDataUsers(
                                participantEntry.participantId!!, meetingId, null, null, null, null
                            ).participantIdMeetingId!!
                        ) ?: throw Error("ResponseParticipant not present")
                        ParticipantData(
                            Participant(participantEntry),
                            responseParticipantEntry.needed!!,
                            responseParticipantEntry.notNeeded!!,
                            responseParticipantEntry.prepared!!,
                            responseParticipantEntry.notPrepared!!
                        )
                    },
                    meetingEntry.presetQIds!!.map {
                        val presetQEntry = database.getEntry<me.rivia.api.database.entry.PresetQ>(
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
