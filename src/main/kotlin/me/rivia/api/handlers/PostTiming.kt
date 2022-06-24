package me.rivia.api.handlers

import me.rivia.api.Response
import me.rivia.api.ResponseError
import me.rivia.api.database.*
import me.rivia.api.database.entry.Usage
import me.rivia.api.graphhttp.MicrosoftGraphAccessClient
import me.rivia.api.teams.TeamsClient
import me.rivia.api.userstore.UserStore
import me.rivia.api.websocket.WebsocketClient

class PostTiming : SubHandler {
    override fun handleRequest(
        url: List<String>,
        tenantId: String?,
        userId: String?,
        validationToken: String,
        jsonData: Map<String, Any?>,
        database: Database,
        userStore: UserStore,
        userAccessToken: TeamsClient,
        applicationAccessToken: TeamsClient,
        graphAccessClient: MicrosoftGraphAccessClient,
        websocket: WebsocketClient
    ): Response {
        val timings = (jsonData["timings"] as? List<*>) ?: return Response(ResponseError.WRONGENTRY)
        val mappedTimings = timings.map { if (it is Double) it else if (it is Int) it.toDouble() else return Response(ResponseError.WRONGENTRY) }
        lateinit var usageEntry: Usage
        do {
            usageEntry = Usage(
                generateUid(),
                tenantId!!,
                userId!!,
                mappedTimings
            )
        } while (!database.putEntry(Table.USAGES, usageEntry))
        return Response()
    }
}
