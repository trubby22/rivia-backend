package me.rivia.api.handlers

import me.rivia.api.Response
import me.rivia.api.database.Database

class PostReview : SubHandler {
    override fun handleRequest(
        url: List<String>,
        tenant: String,
        user: String?,
        jsonData: Map<String, Any?>,
        database: Database
    ): Response {
        TODO("Not yet implemented")
        // If implemented, check if the user is in that tenant (Microsoft Graph)
        // Check if there hasn't been a response from this participant yet (ResponseTenantUsers)
        // Update the feedback list and the quality variables (Meetings)
        // Update responses about participants (ResponseParticipants)
        // Update responses about questions (ResponsePresetQs)
    }
}
