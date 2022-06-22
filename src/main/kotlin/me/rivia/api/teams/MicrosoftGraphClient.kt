package me.rivia.api.teams

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import me.rivia.api.database.Database
import me.rivia.api.database.Table
import me.rivia.api.database.entry.Tenant
import me.rivia.api.database.getEntry
import me.rivia.api.database.updateEntry
import me.rivia.api.graphhttp.MicrosoftGraphAccessClient
import software.amazon.awssdk.http.HttpExecuteRequest
import software.amazon.awssdk.http.SdkHttpMethod
import software.amazon.awssdk.http.SdkHttpRequest
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import java.net.URI

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
            @SerializedName("token_type") val tokenType: String?,
            @SerializedName("expires_in") val expiresIn: Int?,
            @SerializedName("scope") val scope: String?,
            @SerializedName("refresh_token") val refreshToken: String?,
        )

        const val CLIENT_ID = "9661a5c4-6c18-49b8-aad6-e3a4722c2515"
        const val APPLICATION_PERMISSIONS = "ChatMessage.Read.All"
        const val DELEGATED_PERMISSIONS = "ChannelMessage.Send"
        const val REDIRECT_URI = "https://vbc48le64j.execute-api.eu-west-2.amazonaws.com/production"
        const val CLIENT_SECRET = "cap8Q~ESKP.5rpds2UeVfKw39.SB55YSmSwVmag8"
        const val REFRESH_TOKEN_URL = "https://login.microsoftonline.com/common/oauth2/v2.0/token"
    }

    private val client = UrlConnectionHttpClient.builder().build()
    private val jsonParser = Gson()

    private fun getAccessToken(tenantId: String): String {
        val tenant: Tenant? = database.getEntry(
            Table.TENANTS, tenantId
        )
        return if (tokenType == TokenType.USER) tenant!!.userAccessToken!!
        else tenant!!.applicationAccessToken!!
    }

    private fun getRefreshToken(tenantId: String): String? {
        val tenant: Tenant? = database.getEntry(
            Table.TENANTS, tenantId
        )
        return if (tokenType == TokenType.USER) tenant!!.userRefreshToken
        else tenant!!.applicationRefreshToken
    }

    private fun refreshAccessToken(tenantId: String): String? {
        val scope =
            if (tokenType == TokenType.APPLICATION) APPLICATION_PERMISSIONS else DELEGATED_PERMISSIONS
        val body = listOf(
            "client_id" to CLIENT_ID,
            "refresh_token" to getRefreshToken(tenantId)!!,
            "redirect_uri" to REDIRECT_URI,
            "grant_type" to "refresh_token",
            "client_secret" to CLIENT_SECRET
        ).joinToString("&") { (argName, argValue) ->
            "$argName=$argValue"
        }

        val sdkHttpRequest = SdkHttpRequest.builder().uri(
            URI.create(
                REFRESH_TOKEN_URL
            )
        ).putHeader("Content-Type", "application/x-www-form-urlencoded").method(SdkHttpMethod.POST)
            .build()
        val response = client.prepareRequest(
            HttpExecuteRequest.builder().request(
                sdkHttpRequest
            ).contentStreamProvider { body.byteInputStream() }.build()
        ).call()
        if (!response.httpResponse().isSuccessful) {
            return null
        }
        val jsonString = response.responseBody().get().readAllBytes().toString()
        val tokenResponse = jsonParser.fromJson(
            jsonString, TokenResponse::class.java
        )
        setTokens(
            tenantId, tokenResponse.accessToken!!, tokenResponse.refreshToken!!
        )

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

    private fun setUserTokens(tenantId: String, accessToken: String, refreshToken: String) {
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
