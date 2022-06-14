package me.rivia.api.handlers

import me.rivia.api.Response
import me.rivia.api.database.Database

class PostMeeting : SubHandler {
    override fun handleRequest(
        url: List<String>,
        tenant: String,
        user: String?,
        jsonData: Map<String, Any?>,
        database: Database
    ): Response {
        TODO("Not yet implemented")
        // If implemented, check if the user is in that tenant (Microsoft Graph)
        // Add a meeting and return the new id (Meetings)
    }
}
