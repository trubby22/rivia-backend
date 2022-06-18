package me.rivia.api.websocket

interface WebsocketClient {
    fun registerWebsocket(connectionId: String, tenantId: String, userId: String)
    fun unregisterWebsocket(connectionId: String)
    fun sendEvent(tenantId: String, userId: String, event: Any)
}
