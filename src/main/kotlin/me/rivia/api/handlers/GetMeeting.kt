package me.rivia.api.handlers

import me.rivia.api.Response
import me.rivia.api.ResponseError
import me.rivia.api.database.Database
import me.rivia.api.websocket.WebsocketClient
import me.rivia.api.handlers.responses.MeetingId
import me.rivia.api.teams.TeamsClient
import me.rivia.api.userstore.UserStore

class GetMeeting : SubHandler {
    override fun handleRequest(
        url: List<String>,
        tenantId: String,
        userId: String?,
        jsonData: Map<String, Any?>,
        database: Database,
        userStore: UserStore,
        userAccessToken: TeamsClient,
        applicationAccessToken: TeamsClient,
        websocket: WebsocketClient
    ): Response {
        val meetingId = url[1]
        val (meetingEntry, idMeeting) = MeetingId.fetch(database, userStore, meetingId) ?: return Response(
            ResponseError.NOMEETING
        )
        if (tenantId != meetingEntry.tenantId) {
            return Response(ResponseError.WRONGTENANTMEETING)
        }
        if (userId!! !in meetingEntry.userIds!!) {
            return Response(ResponseError.WRONGUSERMEETING)
        }
        return Response(idMeeting.meeting)
    }
}
