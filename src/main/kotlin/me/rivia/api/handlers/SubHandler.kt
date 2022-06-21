package me.rivia.api.handlers

import me.rivia.api.Response
import me.rivia.api.database.Database
import me.rivia.api.teams.TeamsClient
import me.rivia.api.userstore.UserStore
import me.rivia.api.websocket.WebsocketClient

fun interface SubHandler {
    fun handleRequest(
        url: List<String>,
        tenantId: String,
        userId: String?,
        jsonData: Map<String, Any?>,
        database: Database,
        userStore: UserStore,
        userAccessToken: TeamsClient,
        applicationAccessToken: TeamsClient,
        websocket: WebsocketClient
    ): Response
}
