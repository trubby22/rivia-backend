package me.rivia.api.handlers

import me.rivia.api.Response
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
import java.net.URLEncoder
import java.time.OffsetDateTime

class PostSubscription : SubHandler {
    companion object {
        const val certificate = "placeholder"
        const val certificateId = "placeholder"
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
        val request = { token: String ->
            client.prepareRequest(
                HttpExecuteRequest.builder().request(
                    SdkHttpRequest.builder().uri(
                        URI.create(
                            "https://graph.microsoft.com/beta/subscriptions"
                        )
                    ).putHeader("Content-Type", "application/json").putHeader(
                            "Authorization", "Bearer $token"
                        ).method(SdkHttpMethod.POST)
                        .appendRawQueryParameter("change_type", "created")
                        .appendRawQueryParameter(
                            "notificationUrl",
                            "https://vbc48le64j.execute-api.eu-west-2.amazonaws.com/production/graphEvent"
                        ).appendRawQueryParameter(
                            "resource", "/teams/getAllMessages"
                        ).appendRawQueryParameter(
                            "expirationDateTime",
                            OffsetDateTime.now().plusHours(1).utf8()
                        ).appendRawQueryParameter(
                            "includeResourceData", true.utf8()
                        ).appendRawQueryParameter(
                            "encryptionCertificate", certificate.utf8()
                        ).appendRawQueryParameter(
                            "encryptionCertificateId", certificateId.utf8()
                        ).build()
                ).build()
            )
        }

        val response = msClient.tokenOperation(tenantId!!, request).call()
        val inputStream = response.responseBody().get()
        val jsonString = inputStream.readAllBytes().toString()

        return Response(jsonString)
    }

    private fun Any?.utf8(): String =
        URLEncoder.encode(this.toString(), "UTF-8")
}
