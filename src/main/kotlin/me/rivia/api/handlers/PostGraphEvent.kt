package me.rivia.api.handlers

import com.google.gson.annotations.SerializedName
import me.rivia.api.Response
import me.rivia.api.database.Database
import me.rivia.api.teams.TeamsClient
import me.rivia.api.userstore.UserStore
import me.rivia.api.websocket.WebsocketClient

class PostGraphEvent : SubHandler {
    companion object {
        private data class ChangeNotification(
            @SerializedName("value") val value: List<GraphObject>
        )
        private data class GraphObject(
            @SerializedName("resource") val resource: String,
            @SerializedName("resourceData") val resourceData: ResourceData
        )
        private data class ResourceData(
            @SerializedName("@odata.type") val odataType: String
        )
    }
    override fun handleRequest(
        url: List<String>,
        tenantId: String?,
        userId: String?,
        jsonData: Map<String, Any?>,
        database: Database,
        userStore: UserStore,
        userAccessToken: TeamsClient,
        applicationAccessToken: TeamsClient,
        websocket: WebsocketClient
    ): Response {
        val value = jsonData["value"] as List<*>
        val graphObject = value.first()

        TODO("Not yet implemented")
    }
}
