package me.rivia.api.handlers

import me.rivia.api.Response
import me.rivia.api.ResponseError
import me.rivia.api.database.Database
import me.rivia.api.database.Table
import me.rivia.api.database.entry.TenantMeeting
import me.rivia.api.database.getEntry
import me.rivia.api.websocket.WebsocketClient
import me.rivia.api.handlers.responses.Meeting
import me.rivia.api.handlers.responses.MeetingId

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
        if (database.getEntry<TenantMeeting>(
                Table.TENANTMEETINGS,
                TenantMeeting(tenantId, meetingId).tenantIdMeetingId!!
            ) == null
        ) {
            return Response(ResponseError.WRONGTENANTMEETING)
        }
        return Response(MeetingId.fetch(database, meetingId).meeting)
    }
}
