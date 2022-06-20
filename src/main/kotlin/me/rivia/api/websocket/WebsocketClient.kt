package me.rivia.api.websocket

interface WebsocketClient {
    fun registerWebsocket(connectionId: String, tenantId: String, userId: String)
    fun unregisterWebsocket(connectionId: String)
    fun sendEvent(predicate: (String, String) -> Boolean, event: Any)
}
