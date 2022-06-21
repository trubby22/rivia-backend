package me.rivia.api.handlers

import me.rivia.api.Response
import me.rivia.api.ResponseError
import me.rivia.api.database.Database
import me.rivia.api.database.Table
import me.rivia.api.database.entry.User
import me.rivia.api.database.getEntry
import me.rivia.api.teams.TeamsClient
import me.rivia.api.userstore.UserStore
import me.rivia.api.websocket.WebsocketClient

class GetMeetings : SubHandler {
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
        val userEntry = database.getEntry<User>(Table.MEETINGS, User.constructKey(tenantId, userId)) ?: return Response(
            ResponseError.NOTENANT)
        return Response(userEntry.meetingIds!!)
    }
}
