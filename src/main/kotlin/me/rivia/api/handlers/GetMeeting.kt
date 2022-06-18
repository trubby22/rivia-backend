package me.rivia.api.handlers

import me.rivia.api.Response
import me.rivia.api.ResponseError
import me.rivia.api.database.Database
import me.rivia.api.database.Table
import me.rivia.api.database.entry.*
import me.rivia.api.database.entry.Meeting
import me.rivia.api.database.entry.ResponseParticipant
import me.rivia.api.database.entry.TenantMeeting
import me.rivia.api.database.getEntry
import me.rivia.api.websocket.WebsocketClient
import me.rivia.api.handlers.responses.PresetQ as HttpPresetQ
import me.rivia.api.handlers.responses.Participant as HttpParticipant

class GetMeeting : SubHandler {
    override fun handleRequest(
        url: List<String>,
        tenantId: String,
        userId: String?,
        jsonData: Map<String, Any?>,
        database: Database,
        websocket: WebsocketClient
    ): Response {
        val meetingId = url[1]
        val meetingEntry = database.getEntry<Meeting>(Table.MEETINGS, meetingId) ?: return Response(
            ResponseError.WRONGTENANTMEETING
        )
        if (database.getEntry<TenantMeeting>(
                Table.TENANTMEETINGS,
                TenantMeeting(tenantId, meetingId).tenantIdMeetingId!!
            ) == null
        ) {
            return Response(ResponseError.WRONGTENANTMEETING)
        }

        val response = HashMap<String, Any>()
        response["title"] = meetingEntry.title!!
        response["startTime"] = meetingEntry.startTime!!
        response["endTime"] = meetingEntry.endTime!!
        response["qualities"] = meetingEntry.qualities!!
        response["responses"] = meetingEntry.responsesCount!!
        response["organizerId"] = meetingEntry.organizerId!!
        response["participants"] = meetingEntry.participantIds!!.map {
            val participantEntry = database.getEntry<Participant>(Table.PARTICIPANTS, it)
                ?: throw Error("Participant not present")
            val responseParticipantEntry = database.getEntry<ResponseParticipant>(
                Table.RESPONSEPARTICIPANTS,
                ResponseParticipant(
                    participantEntry.participantId!!,
                    meetingId,
                    null,
                    null,
                    null,
                    null
                ).participantIdMeetingId!!
            ) ?: throw Error("ResponseParticipant not present")
            mapOf(
                "participant" to HttpParticipant(participantEntry),
                "needed" to responseParticipantEntry.needed!!,
                "notNeeded" to responseParticipantEntry.notNeeded!!,
                "prepared" to responseParticipantEntry.prepared!!,
                "notPrepared" to responseParticipantEntry.notPrepared!!
            )
        }
        response["presetQs"] = meetingEntry.presetQIds!!.map {
            val presetQEntry =
                database.getEntry<PresetQ>(Table.PRESETQS, it) ?: throw Error("PresetQ not present")
            val responsePresetQEntry = database.getEntry<ResponsePresetQ>(
                Table.RESPONSEPRESETQS,
                ResponsePresetQ(
                    presetQEntry.presetQId!!,
                    meetingId,
                    null,
                    null
                ).presetQIdMeetingId!!
            ) ?: throw Error("responsePresetQ not present")
            mapOf(
                "presetQ" to HttpPresetQ(presetQEntry),
                "submitted" to responsePresetQEntry.numSubmitted!!,
                "selected" to responsePresetQEntry.numSelected!!
            )
        }
        response["feedbacks"] = meetingEntry.feedbacks!!
        return Response(response)
    }
}
