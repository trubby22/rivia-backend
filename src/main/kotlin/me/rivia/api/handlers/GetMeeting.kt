package me.rivia.api.handlers

import me.rivia.api.Response
import me.rivia.api.ResponseError
import me.rivia.api.database.Database
import me.rivia.api.database.Table
import me.rivia.api.database.entry.*
import me.rivia.api.database.entry.Meeting
import me.rivia.api.database.entry.ResponseParticipant
import me.rivia.api.database.entry.ResponseTenantUser
import me.rivia.api.database.entry.Tenant
import me.rivia.api.database.getEntry
import me.rivia.api.database.putEntry
import me.rivia.api.handlers.responses.MeetingResponse
import me.rivia.api.handlers.responses.PresetQ as ResponsePresetQ

class GetMeeting : SubHandler {
    override fun handleRequest(
        url: List<String>,
        tenant: String,
        user: String?,
        jsonData: Map<String, Any?>,
        database: Database
    ): Response {
        TODO("Not yet implemented")
        // If implemented, check if the user is in that tenant (Microsoft Graph)
        // Check if there is such a meeting in that tenant (TenantMeetings)
        // Get the meeting data (Meetings)
        // Get the data about participants (Participants)
        // Get the response data about participants (ResponseParticipants)
        // Get the data about preset questions (PresetQs)
        // Get the response data about preset questions (ResponsePresetQs)
        // Update that the user has already done that response (ResponseTenantUsers)

        // Piotr graph API

        val tenantEntry = database.getEntry<Tenant>(Table.TENANTS, tenant) ?: return Response(ResponseError.NOTENANT)
        val tenantMeeting : TenantMeeting = database.getEntry(Table.TENANTMEETINGS, tenantEntry.tenantId!!) ?: return Response(ResponseError.NONE) // different error here?
        val meetingId = tenantMeeting.meetingId!!
        val meeting : Meeting = database.getEntry(Table.MEETINGS, meetingId) ?: return Response(ResponseError.NONE)
        val participants : List<Participant> = meeting.participantIds!!.map{id -> database.getEntry(Table.PARTICIPANTS, id) ?: throw Error("participantId not found")}
        val resParticipants: List<ResponseParticipant> = meeting.participantIds!!.map{id -> database.getEntry(Table.RESPONSEPARTICIPANTS, id) ?: throw Error("participantId not found")}
        val responsePresetQs = tenantEntry.presetQIds!!.map {ResponsePresetQ(database.getEntry(Table.PRESETQS, it) ?: throw Error("presetQ not found"))}

        val userResponse = ResponseTenantUser(tenant, user!!, meetingId);
        database.putEntry(Table.RESPONSETENANTUSERS, userResponse);

        // need to create map from preset Questions to how many times where selected
        // participant type very similar for request and response
//        return Response(MeetingResponse(meeting.title!!,meeting.startTime!!,meeting.endTime!!,meeting.qualityList!!,
//            meeting.responsesCount!!,meeting.organizerId!!,null,null, meeting.feedbacks!!)
//        )
    }


}
