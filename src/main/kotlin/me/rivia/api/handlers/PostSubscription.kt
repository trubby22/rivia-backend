package me.rivia.api.handlers

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import me.rivia.api.Response
import me.rivia.api.database.Database
import me.rivia.api.database.Table
import me.rivia.api.database.entry.Tenant
import me.rivia.api.database.getAllEntries
import me.rivia.api.graphhttp.MicrosoftGraphAccessClient
import me.rivia.api.teams.TeamsClient
import me.rivia.api.userstore.UserStore
import me.rivia.api.websocket.WebsocketClient
import java.time.OffsetDateTime

class PostSubscription : SubHandler {
    companion object {
        const val CHANGE_TYPE = "created"
        const val NOTIFICATION_URL =
            "https://vbc48le64j.execute-api.eu-west-2.amazonaws.com/production/graphEvent"
        const val SUBSCRIPTION_URL_V1 =
            "https://graph.microsoft.com/v1.0/subscriptions"
        const val SUBSCRIPTION_URL_BETA =
            "https://graph.microsoft.com/beta/subscriptions"
        const val RESOURCE = "/teams/getAllMessages"
        const val CERTIFICATE = """
            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
        """
        const val CERTIFICATE_ID = "myCertificate"

        data class SubscriptionBody(
            @SerializedName("change_type") val changeType: String? = null,
            @SerializedName("notificationUrl") val notificationUrl: String? = null,
            @SerializedName("resource") val resource: String? = null,
            @SerializedName("expirationDateTime") val expirationDateTime: OffsetDateTime? = null,
            @SerializedName("includeResourceData") val includeResourceData: Boolean? = null,
            @SerializedName("encryptionCertificate") val encryptionCertificate: String? = null,
            @SerializedName("encryptionCertificateId") val encryptionCertificateId: String? = null,
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
        jsonData: Map<String, Any?>,
        database: Database,
        userStore: UserStore,
        userAccessToken: TeamsClient,
        applicationAccessToken: TeamsClient,
        graphAccessClient: MicrosoftGraphAccessClient,
        websocket: WebsocketClient
    ): Response {
        val responses = jsonConverter.toJson(
            database.getAllEntries<Tenant>(Table.TENANTS).map { tenant ->
                    renewSubscription(
                        applicationAccessToken,
                        tenant.tenantId,
                        graphAccessClient
                    )
                })

        return Response(responses)
    }

    private fun renewSubscription(
        applicationAccessToken: TeamsClient,
        tenantId: String?,
        graphAccessClient: MicrosoftGraphAccessClient,
    ): RenewResponse {
        val body = jsonConverter.toJson(
            SubscriptionBody(
                expirationDateTime = OffsetDateTime.now().plusMinutes(59)
            )
        )

        return applicationAccessToken.tokenOperation(tenantId!!) { token: String ->
            graphAccessClient.sendRequest(
                url = "${SUBSCRIPTION_URL_V1}/${tenantId}",
                headers = listOf(
                    "Content-Type" to "application/json",
                    "Authorization" to "Bearer $token"
                ),
                method = MicrosoftGraphAccessClient.Companion.HttpMethod.PATCH,
                body = body,
                clazz = RenewResponse::class,
                queryArgs = listOf()
            ) ?: throw Error("Renew subscription request failed")
        }
    }
}
