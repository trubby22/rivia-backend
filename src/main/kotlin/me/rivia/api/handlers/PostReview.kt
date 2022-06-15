package me.rivia.api.handlers

import me.rivia.api.Response
import me.rivia.api.ResponseError
import me.rivia.api.database.Database
import me.rivia.api.database.Table
import me.rivia.api.database.entry.ResponseTenantUser
import me.rivia.api.database.getEntry
import me.rivia.api.database.putEntry

class PostReview : SubHandler {
    override fun handleRequest(
        url: List<String>,
        tenant: String,
        user: String?,
        jsonData: Map<String, Any?>,
        database: Database
    ): Response {
        val meetingId = url[1]

        val needed = jsonData["needed"]
        if (needed !is String?) {
            return Response(ResponseError.WRONGENTRY)
        }
        val notNeeded = jsonData["notNeeded"]
        if (notNeeded !is String?) {
            return Response(ResponseError.WRONGENTRY)
        }
        val prepared = jsonData["prepared"]
        if (prepared !is String?) {
            return Response(ResponseError.WRONGENTRY)
        }
        val notPrepared = jsonData["notPrepared"]
        if (notPrepared !is String?) {
            return Response(ResponseError.WRONGENTRY)
        }
        // should this be a map
        val presetQs = jsonData["presetQs"]
        if (presetQs !is Map<*, *>) {
            return Response(ResponseError.WRONGENTRY)
        }
        val quality = jsonData["quality"]
        if (quality !is Float) {
            return Response(ResponseError.WRONGENTRY)
        }
        val feedback = jsonData["feedback"]
        if (feedback !is String?) {
            return Response(ResponseError.WRONGENTRY)
        }

        // need to check if there hasn't been a response from this participant yet
        val rtu: ResponseTenantUser = ResponseTenantUser(user!!, tenant, meetingId)
        database.putEntry(Table.RESPONSETENANTUSERS, rtu);
        TODO("Not yet implemented")
        // If implemented, check if the user is in that tenant (Microsoft Graph)
        // Check if there hasn't been a response from this participant yet (ResponseTenantUsers)
        // Update the feedback list and the quality variables (Meetings)
        // Update responses about participants (ResponseParticipants)
        // Update responses about questions (ResponsePresetQs)
    }
}
