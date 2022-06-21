package me.rivia.api.teams

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import me.rivia.api.database.Database
import me.rivia.api.database.Table
import me.rivia.api.database.entry.Tenant
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

enum class TokenType {
    APPLICATION,
    USER,
}

class MicrosoftGraphClient(
    private val database: Database,
    private val tokenType: TokenType
) : TeamsClient {
    private val clientId = "9661a5c4-6c18-49b8-aad6-e3a4722c2515"
    private val scope =
        if (tokenType == TokenType.APPLICATION) "ChatMessage.Read.All" else "ChannelMessage.Send"
    private val code = """
        0.AXkA-eTJsC3Xb0CyR-jYbEtBasSlYZYYbLhJqtbjpHIsJRWUAJI.AgABAAIAAAD--DLA3VO7QrddgJg7WevrAgDs_wQA9P-9jdtxaR9Oe_1drpxvprVjYjvAWqaOrNjZE6CMgWFY0jnKQHDZzYcLCs6m5ib017jpmTxsCgoUCf2UIgdKGPnQZCmL8aj4np063jGykZPhVZcj294J0i4Lp2x5Ocp6M3Y1EGrEEpcBHdVEI3EVdaFpqCHGKHHjNiVOuFRVA3sgYwf0oWv83ZVA0Uyfy-SOJEcDvibQZJnwvVm6TVP_3xNtoitFut0Wg5kxzmeAz9wbh-qhDRmOXSFX2yEneAN2kAyUFz9mt_F5StLrR0JaO-thk9XO1sEbfBGU5PR9C7poIs7ZFMPf2r0uir5GcH3SY7OjkQRSl2obIzCq8gJAW3S2UpXIGfB2_FF436SHTcnYqrFPZl8WmNS1K5i-R1Z_ncbMrxh0Vwo-pUtIGDcIhSqi45J0BWrRWjNBk_1Tb_K4sGgeXTDQi03OtMoU3hbvKvNuxqZ9zIq47QoBUgDx6r-RZfwNscNnb_vS7OIhuPq-sRcVFg3CO7jhEZzvh1JSYBFKK6x3lT5xn6D62SspILgpzjeP1un702Hn9LXQEojcNIs0li7M1muGJd4aj1S99zdg7-I6EcyjIwBWdDTnVYUF41clXnxjhogagEJ3eRR_T0BvF-v44MrK4qxPcjIJPvku2HsbB_TqHpDpTMGNxvpUmZT50SafJ1-Z4MVbw8kxDX3y9v0-NVLjtsl3JchX-qWDCboxL2e2l5J4nuwdJ_uvMA
        """
    private val redirectUri = "http://localhost/myapp"
    private val clientSecret = "cap8Q~ESKP.5rpds2UeVfKw39.SB55YSmSwVmag8"
    private var refreshToken: String? = null
    private val client = HttpClient.newBuilder().build()

    private fun String.utf8(): String = URLEncoder.encode(this, "UTF-8")

    private fun getAccessToken(tenantId: String): String {
        val params = mapOf(
            "client_id" to clientId, "scope" to scope, "code"
                    to code, "redirect_uri" to redirectUri, "grant_type" to
                    "authorization_code", "client_secret" to clientSecret
        )
        val urlParams = params.map { (k, v) -> "${k.utf8()}=${v.utf8()}" }
            .joinToString("&")

        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://login.microsoftonline.com/${tenantId}/oauth2/v2.0/token?${urlParams}"))
            .POST(HttpRequest.BodyPublishers.ofString(urlParams))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .build()

        val response =
            client.send(request, HttpResponse.BodyHandlers.ofString())

        val jsonString = response.body()
        val json = Json.parseToJsonElement(jsonString)
        val map = json.jsonObject.toMap()
        val accessToken = map["access_token"].toString()
        refreshToken = map["refresh_token"].toString()

        return accessToken.substring(1, accessToken.length - 1)
    }

    private fun refreshAccessToken(tenantId: String): String? {
        val params = mapOf(
            "client_id" to clientId,
            "scope" to scope,
            "refresh_token"
                    to refreshToken!!,
            "redirect_uri" to redirectUri,
            "grant_type" to "refresh_token",
            "client_secret" to clientSecret
        )
        val urlParams = params.map { (k, v) -> "${k.utf8()}=${v.utf8()}" }
            .joinToString("&")

        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://login.microsoftonline.com/common/oauth2/v2.0/token?${urlParams}"))
            .POST(HttpRequest.BodyPublishers.ofString(urlParams))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .build()

        val response =
            client.send(request, HttpResponse.BodyHandlers.ofString())

        val jsonString = response.body()
        val json = Json.parseToJsonElement(jsonString)
        val map = json.jsonObject.toMap()
        val accessToken = map["access_token"].toString()
        val refreshToken2 = map["refresh_token"].toString()

        return accessToken.substring(1, accessToken.length - 1)
    }

    override fun <T : Any> tokenOperation(
        tenantId: String,
        apiCall: (String) -> T?
    ): T {
        var accessToken: String? = null
//            getAccessToken(tenantId)
        var result = apiCall(accessToken!!)
        while (result == null) {
            do {
                accessToken = refreshAccessToken(tenantId)
            } while (accessToken == null)
            result = apiCall(accessToken)
        }
        return result
    }

    override fun setRefreshToken(tenantId: String, refreshToken: String):
            Boolean {
//        database.updateEntry(Table.TENANTS)
        TODO("Not yet implemented")
    }
}

fun getInfoAboutMe(accessToken: String): String {
    val client = HttpClient.newBuilder().build()
    val request = HttpRequest.newBuilder()
        .uri(URI.create("https://graph.microsoft.com/v1.0/me"))
        .header("Authorization", "Bearer ${accessToken}")
        .build()

    val response =
        client.send(request, HttpResponse.BodyHandlers.ofString())

    return response.body()
}

fun main() {
//    val client = MicrosoftGraphClient(TokenType.USER)
//    val (accessToken, refreshToken) =
//        client.getAccessToken("b0c9e4f9-d72d-406f-b247-e8d86c4b416a")
//    println(client.getInfoAboutMe(accessToken))
//    val res = client.refreshAccessToken(
//        "b0c9e4f9-d72d-406f-b247-e8d86c4b416a",
//        refreshToken
//    )
//    println(res)
}