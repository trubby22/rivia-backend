package me.rivia.api.handlers

import me.rivia.api.Response
import me.rivia.api.database.Database
import me.rivia.api.database.Table
import me.rivia.api.database.entry.ResponseTenantUser
import me.rivia.api.database.getEntry
import me.rivia.api.websocket.WebsocketClient

class GetReview : SubHandler {
    override fun handleRequest(
        url: List<String>,
        tenantId: String,
        userId: String?,
        jsonData: Map<String, Any?>,
        database: Database,
        websocket: WebsocketClient
    ): Response {
        val meetingId = url[1]
        val responseTenantUserEntry = database.getEntry<ResponseTenantUser>(Table.RESPONSETENANTUSERS, ResponseTenantUser(tenantId, userId!!, meetingId).tenantIdUserIdMeetingId!!)
        return Response(responseTenantUserEntry != null)
    }
}
