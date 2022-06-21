package me.rivia.api.handlers

import me.rivia.api.Response
import me.rivia.api.ResponseError
import me.rivia.api.database.*
import me.rivia.api.database.entry.Usage
import me.rivia.api.teams.TeamsClient
import me.rivia.api.userstore.UserStore
import me.rivia.api.websocket.WebsocketClient

class PostTiming : SubHandler {
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
        val timings = (jsonData["timings"] as? List<*>)?.checkListType<Double>() ?: return Response(ResponseError.WRONGENTRY)
        lateinit var usageEntry: Usage
        do {
            usageEntry = Usage(
                generateUid(),
                tenantId,
                userId!!,
                timings
            )
        } while (!database.putEntry(Table.USAGES, usageEntry))
        return Response()
    }
}
