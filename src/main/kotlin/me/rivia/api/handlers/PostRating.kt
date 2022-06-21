package me.rivia.api.handlers

import me.rivia.api.Response
import me.rivia.api.ResponseError
import me.rivia.api.database.*
import me.rivia.api.database.entry.Opinion
import me.rivia.api.teams.TeamsClient
import me.rivia.api.userstore.UserStore
import me.rivia.api.websocket.WebsocketClient

class PostRating : SubHandler {
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
        val like = jsonData["like"] as? Double ?: return Response(ResponseError.WRONGENTRY)
        val use = jsonData["use"] as? Double ?: return Response(ResponseError.WRONGENTRY)
        lateinit var opinionEntry: Opinion
        do {
            opinionEntry = Opinion(
                generateUid(),
                tenantId,
                userId!!,
                like,
                use
            )
        } while (!database.putEntry(Table.OPINIONS, opinionEntry))
        return Response()
    }
}
