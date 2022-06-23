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
        val authorizationCode =
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
            val (newUserAccessToken, newUserRefreshToken) = userAccessToken.fetchAccessToken(
                tenantId,
                userRefreshToken
            ) ?: throw Error("Access token unavailable")
            database.putEntry(Table.TENANTS, Tenant(
                tenantId,
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
}
