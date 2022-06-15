package me.rivia.api.handlers

import me.rivia.api.Response
import me.rivia.api.ResponseError
import me.rivia.api.database.Database

class PostMeeting : SubHandler {
    override fun handleRequest(
        url: List<String>,
        tenant: String,
        user: String?,
        jsonData: Map<String, Any?>,
        database: Database
    ): Response {
        val title = jsonData["title"]
        if (title !is String?) {
            return Response(ResponseError.WRONGENTRY)
        }
        val startTime = jsonData["startTime"]
        if (startTime !is Int?) {
            return Response(ResponseError.WRONGENTRY)
        }
        val endTime = jsonData["endTime"]
        if (endTime !is Int?) {
            return Response(ResponseError.WRONGENTRY)
        }
        val organizer = jsonData["organizer"]
        if (organizer !is Map<*,*>?) {
            return Response(ResponseError.WRONGENTRY)
        }
        // excluding organizer
        val participants = jsonData["participants"]
        if (participants !is List<*>?) {
            return Response(ResponseError.WRONGENTRY)
        }

        TODO("Not yet implemented")
        // If implemented, check if the user is in that tenant (Microsoft Graph)
        // Add a meeting and return the new id (Meetings)
    }
}
