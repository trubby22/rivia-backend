package me.rivia.api.teams

import com.google.gson.annotations.SerializedName
import me.rivia.api.database.Database
import me.rivia.api.database.Table
import me.rivia.api.database.entry.Tenant
import me.rivia.api.database.getEntry
import me.rivia.api.database.updateEntry
import me.rivia.api.graphhttp.MicrosoftGraphAccessClient
import me.rivia.api.graphhttp.sendRequest

enum class TokenType {
    APPLICATION, USER,
}

class MicrosoftGraphClient(
    private val database: Database,
    private val microsoftGraphAccessClient: MicrosoftGraphAccessClient,
    private val tokenType: TokenType
) : TeamsClient {

    companion object {
        private data class TokenResponse(
            @SerializedName("access_token") val accessToken: String?,
            @SerializedName("refresh_token") val refreshToken: String?,
        )

        const val CLIENT_ID = "491d67e2-00cf-46ce-87cc-7e315c09b59f"
        const val REDIRECT_URI =
            "https://app.rivia.me"
        const val CLIENT_SECRET = "xkI8Q~959yh1DI4qWU0BlepJe3TuhERoHgwyjcw-"
        const val APPLICATION_SCOPE = "https://graph.microsoft.com/.default"
        const val DELEGATED_SCOPE = "ChannelMessage.Send"
    }

    private fun getAccessToken(tenantId: String): String {
        val tenant = database.getEntry<Tenant>(
            Table.TENANTS, tenantId
        ) ?: throw Error("Tenant does not exist")
        return if (tokenType == TokenType.USER) tenant.userAccessToken!!
        else tenant.applicationAccessToken!!
    }

    private fun getUserRefreshToken(tenantId: String): String {
        val tenant = database.getEntry<Tenant>(
            Table.TENANTS, tenantId
        ) ?: throw Error("Tenant does not exist")
        return tenant.userRefreshToken!!
    }

    override fun fetchAccessToken(
        tenantId: String,
        userRefreshToken: String?,
    ): Pair<String, String?>? {
        val refreshToken = if (tokenType == TokenType.APPLICATION) null
        else userRefreshToken ?: getUserRefreshToken(tenantId)

        val body = if (tokenType == TokenType.APPLICATION) {
            listOf(
                "client_id" to CLIENT_ID,
                "scope" to APPLICATION_SCOPE,
                "client_secret" to CLIENT_SECRET,
                "grant_type" to "client_credentials"
            )
        } else {
            listOf(
                "client_id" to CLIENT_ID,
                "scope" to DELEGATED_SCOPE,
                "client_secret" to CLIENT_SECRET,
                "refresh_token" to refreshToken,
                "redirect_uri" to REDIRECT_URI,
                "grant_type" to "refresh_token"
            )
        }.joinToString("&") { (argName, argValue) ->
            "$argName=$argValue"
        }
        val tokenResponse =
            microsoftGraphAccessClient.sendRequest<TokenResponse>(
                "https://login.microsoftonline.com/${if (tokenType == TokenType.APPLICATION) tenantId else "common"}/oauth2/v2.0/token",
                listOf(),
                MicrosoftGraphAccessClient.Companion.HttpMethod.POST,
                listOf("Content-Type" to "application/x-www-form-urlencoded"),
                body,
            ) ?: return null

        return Pair(tokenResponse.accessToken!!, tokenResponse.refreshToken)
    }

    private fun refreshAccessToken(
        tenantId: String,
    ): String? {
        val (accessToken, refreshToken) = fetchAccessToken(tenantId) ?: return null
        if (tokenType == TokenType.USER) {
            setUserTokens(
                tenantId,
                accessToken,
                refreshToken ?: throw Error("No refresh token")
            )
        } else {
            setApplicationToken(tenantId, accessToken)
        }
        return accessToken
    }

    override fun <T : Any> tokenOperation(
        tenantId: String, apiCall: (String) -> T?
    ): T {
        var accessToken: String? = getAccessToken(tenantId)
        var result = apiCall(accessToken!!)
        while (result == null) {
            do {
                accessToken = refreshAccessToken(tenantId)
            } while (accessToken == null)
            result = apiCall(accessToken)
        }
        return result
    }

    private fun setUserTokens(
        tenantId: String, accessToken: String, refreshToken: String
    ) {
        database.updateEntry(
            Table.TENANTS, tenantId
        ) { tenantEntry: Tenant ->
            tenantEntry.userAccessToken = accessToken
            tenantEntry.userRefreshToken = refreshToken
            tenantEntry
        } ?: throw Error("Tenant does not exist")
    }

    private fun setApplicationToken(tenantId: String, accessToken: String) {
        database.updateEntry(
            Table.TENANTS, tenantId
        ) { tenantEntry: Tenant ->
            tenantEntry.applicationAccessToken = accessToken
            tenantEntry
        } ?: throw Error("Tenant does not exist")
    }
}
