package me.rivia.api.teams

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import me.rivia.api.database.Database
import me.rivia.api.database.Table
import me.rivia.api.database.entry.Tenant
import me.rivia.api.database.updateEntry
import org.apache.commons.io.IOUtils
import software.amazon.awssdk.http.HttpExecuteRequest
import software.amazon.awssdk.http.SdkHttpMethod
import software.amazon.awssdk.http.SdkHttpRequest
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import java.net.URI
import java.net.URLEncoder

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("expires_in") val expiresIn: Int,
    @SerializedName("scope") val scope: String,
    @SerializedName("refresh_token") val refreshToken: String
)

enum class TokenType {
    APPLICATION, USER,
}

fun Any.utf8(): String = URLEncoder.encode(this.toString(), "UTF-8")

class MicrosoftGraphClient(
    private val database: Database, private val tokenType: TokenType
) : TeamsClient {

    private fun getAccessToken(tenantId: String): String {
        val tenant: Tenant? = database.getEntry<Tenant>(
            Table.TENANTS, tenantId, Tenant::class
        )
        return if (tokenType == TokenType.USER) tenant!!.userAccessToken!!
        else tenant!!.applicationAccessToken!!
    }

    private fun refreshAccessToken(tenantId: String): String? {
        val clientId = "9661a5c4-6c18-49b8-aad6-e3a4722c2515"
        val scope =
            if (tokenType == TokenType.APPLICATION) "ChatMessage.Read.All" else "ChannelMessage.Send"
        val redirectUri = "http://localhost/myapp"
        val clientSecret = "cap8Q~ESKP.5rpds2UeVfKw39.SB55YSmSwVmag8"

        val params = mapOf(
            "client_id" to clientId,
            "scope" to scope,
            "refresh_token"
                    to dbGetRefreshToken(tenantId)!!,
            "redirect_uri" to redirectUri,
            "grant_type" to "refresh_token",
            "client_secret" to clientSecret
        )
        val urlParams =
            params.map { (k, v) -> "${k.utf8()}=${v.utf8()}" }.joinToString("&")

        val client = UrlConnectionHttpClient.builder().build()

        val request = client.prepareRequest(
            HttpExecuteRequest.builder().request
                (
                SdkHttpRequest.builder().uri(
                    URI.create(
                        "https://login.microsoftonline.com/common/oauth2/v2.0/token?${urlParams}"
                    )
                ).putHeader
                    ("Content-Type", "application/x-www-form-urlencoded").method
                    (SdkHttpMethod.POST).build()
            ).build()
        )

        val response = request.call()

        client.close()

        val inputStream = response.responseBody().get()
        val jsonString = IOUtils.toString(inputStream)

        val tokenResponse = Gson().fromJson(
            jsonString,
            TokenResponse::class.java
        )
        val accessToken = tokenResponse.accessToken
        val refreshToken = tokenResponse.refreshToken

        setTokens(tenantId, accessToken, refreshToken)

        return accessToken
    }

    override fun <T : Any> tokenOperation(tenantId: String, apiCall: (String) -> T?): T {
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


    private fun dbGetRefreshToken(tenantId: String): String? {
        val tenant: Tenant? = database.getEntry<Tenant>(
            Table.TENANTS, tenantId, Tenant::class
        )
        return if (tokenType == TokenType.USER) tenant!!.userRefreshToken
        else tenant!!.applicationRefreshToken
    }

    private fun setTokens(
        tenantId: String, accessToken: String,
        refreshToken: String
    ): Boolean {
        val tenant = database.updateEntry(
            Table.TENANTS,
            tenantId
        ) { tenantEntry: Tenant ->
            if (tokenType == TokenType.USER) {
                tenantEntry.userAccessToken = accessToken
                tenantEntry.userRefreshToken = refreshToken
            } else {
                tenantEntry.applicationAccessToken = accessToken
                tenantEntry.userRefreshToken = refreshToken
            }
            tenantEntry
        }
        return tenant != null
    }
}
