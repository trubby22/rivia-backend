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

        const val CLIENT_ID = "9661a5c4-6c18-49b8-aad6-e3a4722c2515"
        const val REDIRECT_URI =
            "https://vbc48le64j.execute-api.eu-west-2.amazonaws.com/production"
        const val CLIENT_SECRET = "cap8Q~ESKP.5rpds2UeVfKw39.SB55YSmSwVmag8"
        const val APPLICATION_PERMISSIONS = "ChatMessage.Read.All"
        const val DELEGATED_PERMISSIONS = "ChannelMessage.Send"
    }

    private fun getAccessToken(tenantId: String): String {
        val tenant: Tenant? = database.getEntry(
            Table.TENANTS, tenantId
        )
        return if (tokenType == TokenType.USER) tenant!!.userAccessToken!!
        else tenant!!.applicationAccessToken!!
    }

    private fun getUserRefreshToken(tenantId: String): String? {
        val tenant: Tenant? = database.getEntry(
            Table.TENANTS, tenantId
        )
        return tenant!!.userRefreshToken
    }

    override fun refreshAccessToken(
        tenantId: String,
        userRefreshToken: String?,
    ): String? {
        val refreshToken = if (tokenType == TokenType.APPLICATION) null
        else userRefreshToken ?: getUserRefreshToken(tenantId)
        ?: throw Error("User refresh token null")

        val body = if (tokenType == TokenType.APPLICATION) {
            listOf(
                "client_id" to CLIENT_ID,
                "scope" to APPLICATION_PERMISSIONS,
                "client_secret" to CLIENT_SECRET,
                "grant_type" to "client_credentials"
            )
        } else {
            listOf(
                "client_id" to CLIENT_ID,
                "scope" to DELEGATED_PERMISSIONS,
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

        if (tokenType == TokenType.USER) {
            setUserTokens(
                tenantId,
                tokenResponse.accessToken!!,
                tokenResponse.refreshToken!!
            )
        } else {
            setApplicationToken(tenantId, tokenResponse.accessToken!!)
        }

        return tokenResponse.accessToken
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
