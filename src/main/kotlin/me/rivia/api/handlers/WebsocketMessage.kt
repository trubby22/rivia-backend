package me.rivia.api.handlers

import me.rivia.api.Response
import me.rivia.api.database.Database
import me.rivia.api.websocket.WebsocketClient

class WebsocketMessage : SubHandler {
    override fun handleRequest(
        url: List<String>,
        tenantId: String,
        userId: String?,
        jsonData: Map<String, Any?>,
        database: Database,
        websocket: WebsocketClient
    ): Response {
        websocket.registerWebsocket(url[1], tenantId, userId!!);
        return Response()
    }
}
