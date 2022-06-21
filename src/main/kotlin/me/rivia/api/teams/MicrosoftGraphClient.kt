package me.rivia.api.teams

//import java.net.http.HttpClient
//import java.net.http.HttpRequest
//import java.net.http.HttpResponse

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

class MicrosoftGraphClient(
    private val database: Database, private val tokenType: TokenType
) : TeamsClient {

    override fun getAccessAndRefreshToken(tenantId: String): Pair<String?, String?> {
        val clientId = "9661a5c4-6c18-49b8-aad6-e3a4722c2515"
        val scope =
            if (tokenType == TokenType.APPLICATION) "ChatMessage.Read.All" else "ChannelMessage.Send"
        val redirectUri = "http://localhost/myapp"
        val clientSecret = "cap8Q~ESKP.5rpds2UeVfKw39.SB55YSmSwVmag8"

        //        val client = HttpClient.newBuilder().build()
        fun String.utf8(): String = URLEncoder.encode(this, "UTF-8")
        val params = mapOf(
            "client_id" to clientId,
            "scope" to scope,
            "refresh_token"
                    to dbGetRefreshToken(tenantId)!!,
            "redirect_uri" to redirectUri,
            "grant_type" to "refresh_token",
            "client_secret" to clientSecret
        )
        val urlParams = params.map { (k, v) -> "${k.utf8()}=${v.utf8()}" }.joinToString("&")

//        val request = HttpRequest.newBuilder()
//            .uri(URI.create("https://login.microsoftonline.com/common/oauth2/v2.0/token?${urlParams}"))
//            .POST(HttpRequest.BodyPublishers.ofString(urlParams))
//            .header("Content-Type", "application/x-www-form-urlencoded")
//            .build()
//
//        val response =
//            client.send(request, HttpResponse.BodyHandlers.ofString())
//        val jsonString = response.body()

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

        return accessToken to refreshToken
    }

    override fun <T : Any> tokenOperation(
        tenantId: String, apiCall: (String) -> T?
    ): T {
        var accessToken = getAccessAndRefreshToken(tenantId).component1()
        var result = apiCall(accessToken!!)
        while (result == null) {
            do {
                accessToken = getAccessAndRefreshToken(tenantId).component1()
            } while (accessToken == null)
            result = apiCall(accessToken)
        }
        return result
    }

    override fun setRefreshToken(tenantId: String, token: String):
            Boolean {
        val tenant = database.updateEntry(
            Table.TENANTS, tenantId
        ) { tenantEntry: Tenant ->
            if (tokenType == TokenType.USER) {
                tenantEntry.userRefreshToken = token
            } else {
                tenantEntry.applicationRefreshToken = token
            }
            tenantEntry
        }
        return tenant != null
    }

    private fun dbGetRefreshToken(tenantId: String): String? {
        val tenant: Tenant? = database.getEntry<Tenant>(
            Table.TENANTS, tenantId, Tenant::class
        )

        return if (tokenType == TokenType.USER) tenant!!.userRefreshToken
        else tenant!!.applicationRefreshToken
    }

    private fun dbGetAccessToken(tenantId: String): String? {
        val tenant: Tenant? = database.getEntry<Tenant>(
            Table.TENANTS,
            tenantId,
            Tenant::class
        )

        return if (tokenType == TokenType.USER) tenant!!.userAccessToken
        else tenant!!.applicationAccessToken
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

