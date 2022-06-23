package me.rivia.api.handlers

import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import me.rivia.api.Response
import me.rivia.api.ResponseError
import me.rivia.api.database.*
import me.rivia.api.database.entry.PresetQ
import me.rivia.api.database.entry.Tenant
import me.rivia.api.graphhttp.MicrosoftGraphAccessClient
import me.rivia.api.graphhttp.sendRequest
import me.rivia.api.teams.MicrosoftGraphClient
import me.rivia.api.teams.TeamsClient
import me.rivia.api.userstore.UserStore
import me.rivia.api.websocket.WebsocketClient
import java.time.OffsetDateTime

class PostLogin : SubHandler {
    companion object {
        const val TOKEN_REDEEM_URL = "https://login.microsoftonline.com/common/oauth2/v2.0/token"

        private data class TokenRedeemBody(
            @SerializedName("access_token") val access_token: String? = null,
            @SerializedName("refresh_token") val refresh_token: String? = null
        )

        const val USER_ID_FETCH_URL = "https://graph.microsoft.com/beta/me"

        private data class UserIdFetchBody(
            @SerializedName("id") val id: String? = null,
        )

        const val TENANT_ID_FETCH_URL = "https://graph.microsoft.com/beta/organization"

        private data class TenantIdFetchBody(
            @SerializedName("id") val id: String? = null
        )
        private data class TenantIdFetchWrapper(
            @SerializedName("value") val value: List<TenantIdFetchBody>? = null,
        )

        // subscription stuff
        const val SUBSCRIPTION_CHANGE_TYPE = "created"
        const val NOTIFICATION_URL =
            "https://api.rivia.me/graphEvent"
        const val SUBSCRIPTION_URL = "https://graph.microsoft.com/beta/subscriptions"
        const val RESOURCE = "teams/getAllMessages"
        const val CERTIFICATE =
            "MIIDSTCCAjGgAwIBAgIIDGx0eZU4388wDQYJKoZIhvcNAQELBQAwUzELMAkGA1UEBhMCeHkxDDAKBgNVBAgTA3h5ejEMMAoGA1UEBxMDeHl6MQwwCgYDVQQKEwN4eXoxDDAKBgNVBAsTA3h5ejEMMAoGA1UEAxMDeHl6MB4XDTIyMDYxMjEzMDM0MloXDTIzMDYxMjEzMDM0MlowUzELMAkGA1UEBhMCeHkxDDAKBgNVBAgTA3h5ejEMMAoGA1UEBxMDeHl6MQwwCgYDVQQKEwN4eXoxDDAKBgNVBAsTA3h5ejEMMAoGA1UEAxMDeHl6MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqkgoLz4BOqbvlEE13yb6JHXMwHmoqw+bJJsNxOoaL2pSWzx4MX7BYbEzV6ZVix1OBEIUKbJAJYYiRQm9elZp5pf77w7xD3PusWOZDEy8s3gvkY1G309qPdYAUS9IarlYKjdc2Dsh3E3+zgQYQWNDdpJWUnJcTxpLpOSmql9nupZKTfRP6VEOBOybnnNJtPmLCLWVqwo5J7urVcuNPcgX7soD3LYdz+mCZZK49xHs0pI/70uEBgdIljp2FItEEYhV1f1UjkGp0pqrE0GHRYxIBmAgyixMUU7romiYBnyCQVP2mYpgD2xH0hVcQ3fEagSG7V5OId7w4ViFx7y7l54lMQIDAQABoyEwHzAdBgNVHQ4EFgQUvcrPGqsLYkkYoTukVMXhIfNjAjEwDQYJKoZIhvcNAQELBQADggEBAGxbWnicAUaHV8hXmy8nQee3wAL7g5rCMwScQCjZxWFTA16c+X0QKWisAoWzRYNGp2t4aPiCiWyuh5tjRiURzVadT+37cMkce6UY74IcYUf8/0zeYI4ut3bEvsJg6pJq9Ak9L6a/KcOm41zK7ehujbUcabMSBd8nQhAiD+1KSkVz8XH7gHAT9EU/CR6Ig43nqbRxyyaVjJLJItdDylMteqdSULRL+5obWK5FgkHmEN40iQaYtcxU0b7apHPRhbB10OX58arEJ405FaSNy0aB8RyAEBKMOR7wz6Nh6THK4U6qDmHf4o4zEd7S7yjHZdPTWxyOS1TW8m5WKJWas+85utA="
        const val CERTIFICATE_ID = "myCertificate"

        private data class SubscriptionBody(
            @SerializedName("changeType") val changeType: String? = null,
            @SerializedName("notificationUrl") val notificationUrl: String? = null,
            @SerializedName("resource") val resource: String? = null,
            @SerializedName("expirationDateTime") val expirationDateTime: String? = null,
            @SerializedName("includeResourceData") val includeResourceData: String? = null,
            @SerializedName("encryptionCertificate") val encryptionCertificate: String? = null,
            @SerializedName("encryptionCertificateId") val encryptionCertificateId: String? = null,
        )

        private data class SubscriptionResponse(
            @SerializedName("id") val id: String? = null
        )

        private data class SubscriptionList(
            @SerializedName("value") val value: List<SubscriptionResponse>? = null
        )
    }

    private val jsonConverter = GsonBuilder()
        .disableHtmlEscaping()
        .create()

    override fun handleRequest(
        url: List<String>,
        _tenantId: String?,
        _userId: String?,
        validationToken: String,
        jsonData: Map<String, Any?>,
        database: Database,
        userStore: UserStore,
        userAccessToken: TeamsClient,
        applicationAccessToken: TeamsClient,
        graphAccessClient: MicrosoftGraphAccessClient,
        websocket: WebsocketClient
    ): Response {
        // Redeeming the token
        var authorizationCode =
            jsonData["authorizationCode"] as? String ?: Response(ResponseError.WRONGENTRY)

        val tokenRedeemBody = listOf(
            "client_id" to MicrosoftGraphClient.CLIENT_ID,
            "grant_type" to "authorization_code",
            "scope" to MicrosoftGraphClient.DELEGATED_SCOPE,
            "code" to authorizationCode,
            "redirect_uri" to MicrosoftGraphClient.REDIRECT_URI,
            "client_secret" to MicrosoftGraphClient.CLIENT_SECRET
        ).joinToString("&") { (argName, argValue) ->
            "$argName=$argValue"
        }

        val tokenRedeemResponse = graphAccessClient.sendRequest<TokenRedeemBody>(
            TOKEN_REDEEM_URL,
            listOf(),
            MicrosoftGraphAccessClient.Companion.HttpMethod.POST,
            listOf(
                "Content-Type" to "application/x-www-form-urlencoded",
                "Accept" to "application/json"
            ),
            tokenRedeemBody
        ) ?: throw Error("Token fetching failed")
        val userRefreshToken = tokenRedeemResponse.refresh_token

        // Getting the user and tenant id
        val tenantIdResponse = graphAccessClient.sendRequest<TenantIdFetchWrapper>(
            TENANT_ID_FETCH_URL,
            listOf(),
            MicrosoftGraphAccessClient.Companion.HttpMethod.GET,
            listOf(
                "Authorization" to "Bearer ${tokenRedeemResponse.access_token}",
                "Accept" to "application/json"
            ),
            null
        ) ?: throw Error("Tenant fetching failed")
        val tenantId = tenantIdResponse.value!![0].id

        val userIdResponse = graphAccessClient.sendRequest<UserIdFetchBody>(
            USER_ID_FETCH_URL,
            listOf(),
            MicrosoftGraphAccessClient.Companion.HttpMethod.GET,
            listOf(
                "Authorization" to "Bearer ${tokenRedeemResponse.access_token}",
                "Accept" to "application/json"
            ),
            null
        ) ?: throw Error("Tenant fetching failed")
        val userId = userIdResponse.id

        // Inserting the tenant entry
        if (database.getEntry<Tenant>(Table.TENANTS, tenantId!!) == null) {
            val subscriptionId = createSubscription(
                graphAccessClient, tenantId, applicationAccessToken
            )
            val (newUserAccessToken, newUserRefreshToken) = userAccessToken.fetchAccessToken(
                tenantId,
                userRefreshToken
            ) ?: throw Error("Access token unavailable")
            database.putEntry(Table.TENANTS, Tenant(
                tenantId,
                subscriptionId,
                applicationAccessToken.fetchAccessToken(tenantId)?.first
                    ?: throw Error("Access token unavailable"),
                newUserRefreshToken,
                newUserAccessToken,
                database.getAllEntries<PresetQ>(Table.PRESETQS).filter {
                    it.isdefault!!
                }.map { it.presetQId!! }
            ))
        }
        return Response(mapOf("tenantId" to tenantId, "userId" to userId))
    }

    private fun createSubscription(
        graphAccessClient: MicrosoftGraphAccessClient,
        tenantId: String,
        applicationAccessToken: TeamsClient,
    ): String {
        val body = jsonConverter.toJson(
            SubscriptionBody(
                changeType = SUBSCRIPTION_CHANGE_TYPE,
                notificationUrl = NOTIFICATION_URL,
                resource = RESOURCE,
                expirationDateTime = OffsetDateTime.now().plusMinutes(5).toString(),
                includeResourceData = true.toString(),
                encryptionCertificate = CERTIFICATE,
                encryptionCertificateId = CERTIFICATE_ID
            )
        )

        return graphAccessClient.sendRequest<SubscriptionResponse>(
            SUBSCRIPTION_URL,
            listOf(),
            MicrosoftGraphAccessClient.Companion.HttpMethod.POST,
            listOf(
                "Content-Type" to "application/json",
                "Accept" to "application/json",
                "Authorization" to "Bearer ${
                    applicationAccessToken.fetchAccessToken(tenantId)?.first ?: throw Error("Access token unavailable")
                }"
            ),
            body
        )?.id ?: throw Error("Subscription failed")
    }
}
