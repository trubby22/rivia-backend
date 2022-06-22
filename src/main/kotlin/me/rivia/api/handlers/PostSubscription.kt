package me.rivia.api.handlers

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import me.rivia.api.Response
import me.rivia.api.database.Database
import me.rivia.api.database.Table
import me.rivia.api.database.entry.Tenant
import me.rivia.api.database.getAllEntries
import me.rivia.api.graphhttp.*
import me.rivia.api.teams.TeamsClient
import me.rivia.api.userstore.UserStore
import me.rivia.api.websocket.WebsocketClient
import java.time.OffsetDateTime

class PostSubscription : SubHandler {
    companion object {
        private data class RenewBody(
            @SerializedName("expirationDateTime") val expirationDateTime: OffsetDateTime? = null,
        )

        private data class RenewResponse(
            @SerializedName("expirationDateTime") val expirationDateTime: OffsetDateTime?
        )
    }

    private val jsonConverter = Gson()

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
        for (tenant in database.getAllEntries<Tenant>(Table.TENANTS)) {
            renewSubscription(
                applicationAccessToken,
                tenant.tenantId!!,
                tenant.subscriptionId!!,
                graphAccessClient
            )
        }

        return Response()
    }

    private fun renewSubscription(
        applicationAccessToken: TeamsClient,
        tenantId: String,
        subscriptionId: String,
        graphAccessClient: MicrosoftGraphAccessClient,
    ) {
        val body = jsonConverter.toJson(
            RenewBody(
                expirationDateTime = OffsetDateTime.now().plusMinutes(59)
            )
        )

        applicationAccessToken.tokenOperation(tenantId!!) { token: String ->
            graphAccessClient.sendRequest<RenewResponse>(
                "https://graph.microsoft.com/beta/subscriptions/${subscriptionId}",
                listOf(),
                MicrosoftGraphAccessClient.Companion.HttpMethod.PATCH,
                listOf(
                    "Content-Type" to "application/json", "Authorization" to "Bearer $token"
                ),
                body,
            )
        }
    }
}
