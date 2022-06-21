package me.rivia.api.handlers

import me.rivia.api.Response
import me.rivia.api.database.Database
import me.rivia.api.teams.MicrosoftGraphClient
import me.rivia.api.teams.TeamsClient
import me.rivia.api.teams.TokenType
import me.rivia.api.teams.utf8
import me.rivia.api.userstore.UserStore
import me.rivia.api.websocket.WebsocketClient
import software.amazon.awssdk.http.HttpExecuteRequest
import software.amazon.awssdk.http.SdkHttpMethod
import software.amazon.awssdk.http.SdkHttpRequest
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import java.net.URI
import java.time.OffsetDateTime

class PostSubscription : SubHandler {
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

        val params = mapOf(
            "change_type" to "created",
            "notificationUrl" to
                    "https://vbc48le64j.execute-api.eu-west-2.amazonaws.com/production/graphEvent",
            "resource" to "/teams/getAllMessages",
            "expirationDateTime" to OffsetDateTime.now().plusHours(1),
            "includeResourceData" to true,
            "encryptionCertificate" to null,
            "encryptionCertificateId" to null,
        )
        val urlParams =
            params.map { (k, v) -> "${k.utf8()}=${v!!.utf8()}" }.joinToString("&")

        val client = UrlConnectionHttpClient.builder().build()

        val msClient = MicrosoftGraphClient(
            database, TokenType
                .APPLICATION
        )

        val request = { token: String ->
            client.prepareRequest(
                HttpExecuteRequest.builder().request
                    (
                    SdkHttpRequest.builder().uri(
                        URI.create(
                            "https://graph.microsoft.com/beta/subscriptions?${urlParams}"
                        )
                    )
                        .putHeader("Content-Type", "application/json")
                        .putHeader(
                            "Authorization", "Bearer $token"
                        )
                        .method(SdkHttpMethod.POST).build()
                ).build()
            )
        }

        val response = msClient.tokenOperation(tenantId!!, request).call()

        client.close()

        val inputStream = response.responseBody().get()
        val jsonString = inputStream.readAllBytes().toString()

        return Response(jsonString)
    }
}
