package me.rivia.api.handlers

import me.rivia.api.Response
import me.rivia.api.database.Database

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
    }
}
