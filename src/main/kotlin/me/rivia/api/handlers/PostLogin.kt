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
            "MIIFyzCCA7OgAwIBAgIUa+1Fd0PjMGEcdl1Jl4pFd3VlW/owDQYJKoZIhvcNAQELBQAwdTELMAkGA1UEBhMCVUsxDzANBgNVBAgMBkxvbmRvbjEPMA0GA1UEBwwGTG9uZG9uMREwDwYDVQQKDAhSaXZpYUFwcDEOMAwGA1UEAwwFUGlvdHIxITAfBgkqhkiG9w0BCQEWEnJpdmlvYXBwQGdtYWlsLmNvbTAeFw0yMjA2MjIwOTI4MTdaFw0yNTA2MjEwOTI4MTdaMHUxCzAJBgNVBAYTAlVLMQ8wDQYDVQQIDAZMb25kb24xDzANBgNVBAcMBkxvbmRvbjERMA8GA1UECgwIUml2aWFBcHAxDjAMBgNVBAMMBVBpb3RyMSEwHwYJKoZIhvcNAQkBFhJyaXZpb2FwcEBnbWFpbC5jb20wggIiMA0GCSqGSIb3DQEBAQUAA4ICDwAwggIKAoICAQDgWB0pNZWfAI8iGa5YyzAWErF9u8GM1t2VVWIW7ajWXLt7j9eOfK9eov2TSOImzdbsm2VqrJa4bWxjH4fzRUbZmCcgy5jt2iMcCJEdhFHFtANJqdGC55KHeOQWf2bGLXmbNnMkee+6kh5Cv0PRCAp75teoEnpmjMkvWFTA9rJdoWrgxpxdbc20GA74yqcQzTUjQ3eOurJFu+9oRPRaXgzdzyTmDjDFIDSDE3k8imA+p1c4rZqtiF8KE/sTGQc32Y6jO+///mYi+AVOCZoV0pVzWHkJkW8lMMiFJNjHdUn6QasS+m0LflS5X+fb2E3gMx2btV0iSFXEVUHiMhyexfsrTo440BTqeP99zKzic3azWV8zTCVTy0FW6x2jBsvS5cbQG9xoNhm7ZLYJhER191rDoYjeoz2rBzajISrgtik696bESpPSJDD2LlzL8AMesE8nXUPrw9pPnzDwJ7NeD0yvZmyy/19BOgxC2RlM066iYikxmPhYSZKoDwIwaexLo3QZRxnUk97fK48zwkxl5E8XbuEOhSlXV4R7IggewEHvy/rjBNgev1OF72zUx+wBqhTAkJXRl2q1G+GjVSz2F0QN2Hv27SowVFq/Sol3T1jREcbh1lbdLIevuiZOj4CBocBiOfHaBP48Xr9kUtq4CPCdQ0UC0H3VnWD8SmWah5AkowIDAQABo1MwUTAdBgNVHQ4EFgQUE1XZnOLMD5H14XEZPQQDXdLSyoUwHwYDVR0jBBgwFoAUE1XZnOLMD5H14XEZPQQDXdLSyoUwDwYDVR0TAQH/BAUwAwEB/zANBgkqhkiG9w0BAQsFAAOCAgEAzPxtYP1DnNXtLhkVYOjvs42X0LUCle1wkhEwdzBpi7POi2eB+cItC1AsTwraeQiiTAOK60AhmlORVWYBsbbrjGel9IUo9ZyWRwZpQMzOt8Yr+X7NmsAdmTPFWBHtbm1JTBS44evL55tZOaPOCim5vj4Mh96zWEzoG8/LEnbeP13t3lEfgw/rn9aHVDuzYMf2KYbBb+PQbclBq9k+dVIJaMRC3CIvJi8yzA96uwFo4pLe0yoZAP6AZvqmNszpVJSBdZisbkvaZhB/TY6XfQBgh62SBI1aZyi8Wy3aFbpdPi4gl//5IjtT/uJcDBVtHoejHq4C+miV1r5iTMfx9i+JpudGFLkzTbYd5o+9TD4VgSSdm7BXbUiFqUQWXFMWrspQd2DATX+pvVmdJ0+hSWtG1yEJmdfaiueg5drb+jQoTSF2GdKa2i3BsrKsy9pbfcsBsNTm/+rUJCTn5ZRhXr+rD5Hosiy5IyGj4MJSQ5BYpTJy8UVe/54nX5pYRvt10Qs94zcd1lF2q6QMns5AKQhGUnqJBQrx4ZzbSRPknME4Po1Ej1VIAr+M2RCz+ZlVuYskVBmyYTdPb5WvqqhfHwJPzHpsbyjN6J6R2jBruSUQmu+DL0eCTlPnb5BU5sKJWRsaahXirqCjHx8hOlWaypqbODcRKkSS4haLBDBzYS1gBaQ="
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
            @SerializedName("id") val id: String
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
                expirationDateTime = OffsetDateTime.now().plusMinutes(59).toString(),
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
