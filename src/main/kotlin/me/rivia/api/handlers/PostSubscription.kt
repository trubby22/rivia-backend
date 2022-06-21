package me.rivia.api.handlers

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import me.rivia.api.Response
import me.rivia.api.ResponseError
import me.rivia.api.database.Database
import me.rivia.api.teams.MicrosoftGraphClient
import me.rivia.api.teams.TeamsClient
import me.rivia.api.teams.TokenType
import me.rivia.api.userstore.UserStore
import me.rivia.api.websocket.WebsocketClient
import software.amazon.awssdk.http.HttpExecuteRequest
import software.amazon.awssdk.http.SdkHttpMethod
import software.amazon.awssdk.http.SdkHttpRequest
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import java.net.URI
import java.time.OffsetDateTime

class PostSubscription : SubHandler {
    companion object {
        const val CHANGE_TYPE = "created"
        const val NOTIFICATION_URL =
            "https://vbc48le64j.execute-api.eu-west-2.amazonaws.com/production/graphEvent"
        const val RESOURCE = "/teams/getAllMessages"
        const val CERTIFICATE = "placeholder"
        const val CERTIFICATE_ID = "placeholder"

        private data class SubscriptionBody(
            @SerializedName("change_type") val changeType: String?,
            @SerializedName("notificationUrl") val notificationUrl: String?,
            @SerializedName("resource") val resource: String?,
            @SerializedName("expirationDateTime") val expirationDateTime: OffsetDateTime?,
            @SerializedName("includeResourceData") val includeResourceData: Boolean?,
            @SerializedName("encryptionCertificate") val encryptionCertificate: String?,
            @SerializedName("encryptionCertificateId") val encryptionCertificateId: String?,
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
        val client = UrlConnectionHttpClient.builder().build()
        val msClient = MicrosoftGraphClient(
            database, TokenType.APPLICATION
        )
        val jsonConverter = Gson()
        val body = jsonConverter.toJson(
            SubscriptionBody(
                changeType = CHANGE_TYPE,
                notificationUrl = NOTIFICATION_URL,
                resource = RESOURCE,
                expirationDateTime = OffsetDateTime.now().plusMinutes(59),
                includeResourceData = true,
                encryptionCertificate = CERTIFICATE,
                encryptionCertificateId = CERTIFICATE_ID
            )
        )

        val response = msClient.tokenOperation(tenantId!!) { token: String ->
            client.prepareRequest(
                HttpExecuteRequest.builder().request(
                    SdkHttpRequest.builder().uri(
                        URI.create(
                            "https://graph.microsoft.com/beta/subscriptions"
                        )
                    ).putHeader("Content-Type", "application/json").putHeader(
                        "Authorization", "Bearer $token"
                    ).method(SdkHttpMethod.POST).build()
                ).contentStreamProvider { body.byteInputStream() }.build()
            )
        }.call()
        if (!response.httpResponse().isSuccessful) {
            return Response(ResponseError.EXCEPTION)
        }
        return Response(response.responseBody().get().readAllBytes().toString())
    }
}
