package me.rivia.api.handlers

import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import me.rivia.api.Response
import me.rivia.api.ResponseError
import me.rivia.api.database.*
import me.rivia.api.database.entry.PresetQ
import me.rivia.api.database.entry.Tenant
import me.rivia.api.encryption.CertificateStoreService
import me.rivia.api.graphhttp.MicrosoftGraphAccessClient
import me.rivia.api.graphhttp.MicrosoftGraphHttpClient
import me.rivia.api.graphhttp.sendRequest
import me.rivia.api.teams.MicrosoftGraphClient
import me.rivia.api.teams.TeamsClient
import me.rivia.api.userstore.UserStore
import me.rivia.api.websocket.WebsocketClient
import java.time.OffsetDateTime

class PostLogin : SubHandler {
    companion object {
        const val TOKEN_REDEEM_URL =
            "https://login.microsoftonline.com/common/oauth2/v2.0/token"

        private data class TokenRedeemBody(
            @SerializedName("access_token") val access_token: String? = null,
            @SerializedName("refresh_token") val refresh_token: String? = null
        )

        const val USER_ID_FETCH_URL = "https://graph.microsoft.com/beta/me"

        private data class UserIdFetchBody(
            @SerializedName("id") val id: String? = null,
        )

        const val TENANT_ID_FETCH_URL =
            "https://graph.microsoft.com/beta/organization"

        private data class TenantIdFetchBody(
            @SerializedName("id") val id: String? = null
        )

        private data class TenantIdFetchWrapper(
            @SerializedName("value") val value: List<TenantIdFetchBody>? = null,
        )

        // subscription stuff
        const val SUBSCRIPTION_CHANGE_TYPE = "created"
        const val NOTIFICATION_URL = "https://api.rivia.me/graphEvent"
        const val SUBSCRIPTION_URL =
            "https://graph.microsoft.com/beta/subscriptions"
        const val RESOURCE = "teams/getAllMessages"

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

    private val jsonConverter = GsonBuilder().disableHtmlEscaping().create()

    private val certificateStore = CertificateStoreService()

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
        var authorizationCode = jsonData["authorizationCode"] as? String
            ?: Response(ResponseError.WRONGENTRY)

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

        val tokenRedeemResponse =
            graphAccessClient.sendRequest<TokenRedeemBody>(
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
        val tenantIdResponse =
            graphAccessClient.sendRequest<TenantIdFetchWrapper>(
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
                graphAccessClient, tenantId,
//                applicationAccessToken
                ""
            )
            val (newUserAccessToken, newUserRefreshToken) = userAccessToken.fetchAccessToken(
                tenantId, userRefreshToken
            ) ?: throw Error("Access token unavailable")
            database.putEntry(Table.TENANTS, Tenant(tenantId,
                subscriptionId,
                applicationAccessToken.fetchAccessToken(tenantId)?.first
                    ?: throw Error("Access token unavailable"),
                newUserRefreshToken,
                newUserAccessToken,
                database.getAllEntries<PresetQ>(Table.PRESETQS).filter {
                    it.isdefault!!
                }.map { it.presetQId!! }))
        }
        return Response(mapOf("tenantId" to tenantId, "userId" to userId))
    }

    fun createSubscription(
        graphAccessClient: MicrosoftGraphAccessClient, tenantId: String,
//        applicationAccessToken: TeamsClient,
        accessToken: String
    ): String {
        val body = jsonConverter.toJson(
            SubscriptionBody(
                changeType = SUBSCRIPTION_CHANGE_TYPE,
                notificationUrl = NOTIFICATION_URL,
                resource = RESOURCE,
                expirationDateTime = OffsetDateTime.now().plusMinutes(5)
                    .toString(),
                includeResourceData = true.toString(),
                encryptionCertificate = certificateStore.base64EncodedCertificate,
                encryptionCertificateId = certificateStore.certificateId
            )
        )

        val response = graphAccessClient.sendRequest<SubscriptionResponse>(
            SUBSCRIPTION_URL,
            listOf(),
            MicrosoftGraphAccessClient.Companion.HttpMethod.POST,
            listOf(
                "Content-Type" to "application/json",
                "Accept" to "application/json",
                "Authorization" to "Bearer ${
//                    applicationAccessToken.fetchAccessToken(tenantId)?.first ?: throw Error("Access token unavailable")
                    accessToken
                }"
            ),
            body
        )

        return ""
    }
}

fun main() {
    PostLogin().createSubscription(
        MicrosoftGraphHttpClient(),
        "b0c9e4f9-d72d-406f-b247-e8d86c4b416a",
        "eyJ0eXAiOiJKV1QiLCJub25jZSI6IkYzWnZUMnZ6NmxtUlNOa1ViT3BMM3o0am9aSkZZMlJ5VXQtRG9va2FfSFUiLCJhbGciOiJSUzI1NiIsIng1dCI6IjJaUXBKM1VwYmpBWVhZR2FYRUpsOGxWMFRPSSIsImtpZCI6IjJaUXBKM1VwYmpBWVhZR2FYRUpsOGxWMFRPSSJ9.eyJhdWQiOiJodHRwczovL2dyYXBoLm1pY3Jvc29mdC5jb20iLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC9iMGM5ZTRmOS1kNzJkLTQwNmYtYjI0Ny1lOGQ4NmM0YjQxNmEvIiwiaWF0IjoxNjU2MDc4NzU5LCJuYmYiOjE2NTYwNzg3NTksImV4cCI6MTY1NjA4MjY1OSwiYWlvIjoiRTJaZ1lBaHhpL1BKMlpVZnNmYU1obkhlTTVmYkFBPT0iLCJhcHBfZGlzcGxheW5hbWUiOiJKYXZhIFNwcmluZyBHcmFwaCBOb3RpZmljYXRpb24gV2ViaG9vayBTYW1wbGUgMiIsImFwcGlkIjoiODgxNGM0MzYtMjBiOC00YTRkLWFiNWYtYzQ5ZDY4MzdjMDAwIiwiYXBwaWRhY3IiOiIxIiwiaWRwIjoiaHR0cHM6Ly9zdHMud2luZG93cy5uZXQvYjBjOWU0ZjktZDcyZC00MDZmLWIyNDctZThkODZjNGI0MTZhLyIsImlkdHlwIjoiYXBwIiwib2lkIjoiYTY5Y2U0MWEtMTk1Zi00NGM1LTk0ZDgtMjE1ZjBmMDE4NjFhIiwicmgiOiIwLkFYa0EtZVRKc0MzWGIwQ3lSLWpZYkV0QmFnTUFBQUFBQUFBQXdBQUFBQUFBQUFDVUFBQS4iLCJyb2xlcyI6WyJBZ3JlZW1lbnQuUmVhZFdyaXRlLkFsbCIsIkNoYXQuVXBkYXRlUG9saWN5VmlvbGF0aW9uLkFsbCIsIkN1c3RvbUF1dGhlbnRpY2F0aW9uRXh0ZW5zaW9uLlJlY2VpdmUuUGF5bG9hZCIsIlBvbGljeS5SZWFkLkNvbmRpdGlvbmFsQWNjZXNzIiwiQXBwQ2F0YWxvZy5SZWFkLkFsbCIsIlRlYW13b3JrLk1pZ3JhdGUuQWxsIiwiQ2FsbHMuSm9pbkdyb3VwQ2FsbC5BbGwiLCJBZ3JlZW1lbnRBY2NlcHRhbmNlLlJlYWQuQWxsIiwiQVBJQ29ubmVjdG9ycy5SZWFkV3JpdGUuQWxsIiwiUG9saWN5LlJlYWRXcml0ZS5QZXJtaXNzaW9uR3JhbnQiLCJDbG91ZFBDLlJlYWQuQWxsIiwiT25saW5lTWVldGluZ3MuUmVhZC5BbGwiLCJBY2Nlc3NSZXZpZXcuUmVhZFdyaXRlLk1lbWJlcnNoaXAiLCJQb2xpY3kuUmVhZFdyaXRlLkNvbmRpdGlvbmFsQWNjZXNzIiwiQml0bG9ja2VyS2V5LlJlYWRCYXNpYy5BbGwiLCJQb2xpY3kuUmVhZFdyaXRlLkF1dGhlbnRpY2F0aW9uTWV0aG9kIiwiQml0bG9ja2VyS2V5LlJlYWQuQWxsIiwiQXV0aGVudGljYXRpb25Db250ZXh0LlJlYWQuQWxsIiwiT25saW5lTWVldGluZ3MuUmVhZFdyaXRlLkFsbCIsIlBvbGljeS5SZWFkLlBlcm1pc3Npb25HcmFudCIsIkNoYW5uZWwuRGVsZXRlLkFsbCIsIk9ubGluZU1lZXRpbmdBcnRpZmFjdC5SZWFkLkFsbCIsIlBvbGljeS5SZWFkV3JpdGUuQXV0aGVudGljYXRpb25GbG93cyIsIkFwcGxpY2F0aW9uLlJlYWRXcml0ZS5Pd25lZEJ5IiwiQm9va2luZ3NBcHBvaW50bWVudC5SZWFkV3JpdGUuQWxsIiwiQ2hhbm5lbFNldHRpbmdzLlJlYWQuQWxsIiwiQXBwQ2F0YWxvZy5SZWFkV3JpdGUuQWxsIiwiQ3Jvc3NUZW5hbnRVc2VyUHJvZmlsZVNoYXJpbmcuUmVhZC5BbGwiLCJDYWxlbmRhcnMuUmVhZCIsIkNoYXRNZW1iZXIuUmVhZFdyaXRlLkFsbCIsIlBvbGljeS5SZWFkV3JpdGUuQXBwbGljYXRpb25Db25maWd1cmF0aW9uIiwiQ2hhbm5lbC5SZWFkQmFzaWMuQWxsIiwiQ2FsbHMuSW5pdGlhdGVHcm91cENhbGwuQWxsIiwiR3JvdXAuUmVhZC5BbGwiLCJBZG1pbmlzdHJhdGl2ZVVuaXQuUmVhZC5BbGwiLCJDdXN0b21BdXRoZW50aWNhdGlvbkV4dGVuc2lvbi5SZWFkLkFsbCIsIkFjY2Vzc1Jldmlldy5SZWFkV3JpdGUuQWxsIiwiRGlyZWN0b3J5LlJlYWRXcml0ZS5BbGwiLCJQb2xpY3kuUmVhZFdyaXRlLkNvbnNlbnRSZXF1ZXN0IiwiQ3Jvc3NUZW5hbnRJbmZvcm1hdGlvbi5SZWFkQmFzaWMuQWxsIiwiQ3VzdG9tQXV0aGVudGljYXRpb25FeHRlbnNpb24uUmVhZFdyaXRlLkFsbCIsIkNhbGxzLkpvaW5Hcm91cENhbGxBc0d1ZXN0LkFsbCIsIkF1dGhlbnRpY2F0aW9uQ29udGV4dC5SZWFkV3JpdGUuQWxsIiwiQ29udGFjdHMuUmVhZFdyaXRlIiwiR3JvdXAuUmVhZFdyaXRlLkFsbCIsIkJvb2tpbmdzLlJlYWQuQWxsIiwiQ2FsbFJlY29yZHMuUmVhZC5BbGwiLCJDaGF0TWVzc2FnZS5SZWFkLkFsbCIsIkNvbnNlbnRSZXF1ZXN0LlJlYWQuQWxsIiwiQ2FsbFJlY29yZC1Qc3RuQ2FsbHMuUmVhZC5BbGwiLCJVc2VyLlJlYWQuQWxsIiwiQWdyZWVtZW50LlJlYWQuQWxsIiwiQ2hhbm5lbE1lbWJlci5SZWFkLkFsbCIsIkN1c3RvbVNlY0F0dHJpYnV0ZUFzc2lnbm1lbnQuUmVhZC5BbGwiLCJBUElDb25uZWN0b3JzLlJlYWQuQWxsIiwiQ29uc2VudFJlcXVlc3QuUmVhZFdyaXRlLkFsbCIsIkFwcFJvbGVBc3NpZ25tZW50LlJlYWRXcml0ZS5BbGwiLCJDaGF0LlJlYWQuQWxsIiwiQ2hhbm5lbE1lc3NhZ2UuUmVhZC5BbGwiLCJDYWxlbmRhcnMuUmVhZFdyaXRlIiwiQWNjZXNzUmV2aWV3LlJlYWQuQWxsIiwiQ3Jvc3NUZW5hbnRVc2VyUHJvZmlsZVNoYXJpbmcuUmVhZFdyaXRlLkFsbCIsIkNoYXQuUmVhZFdyaXRlLkFsbCIsIkNoYW5uZWxNZXNzYWdlLlVwZGF0ZVBvbGljeVZpb2xhdGlvbi5BbGwiLCJDaGFubmVsTWVtYmVyLlJlYWRXcml0ZS5BbGwiLCJDb250YWN0cy5SZWFkIiwiQ2hhbm5lbFNldHRpbmdzLlJlYWRXcml0ZS5BbGwiLCJBZG1pbmlzdHJhdGl2ZVVuaXQuUmVhZFdyaXRlLkFsbCIsIkF1ZGl0TG9nLlJlYWQuQWxsIiwiQ2hhbm5lbC5DcmVhdGUiLCJQb2xpY3kuUmVhZC5BbGwiLCJQb2xpY3kuUmVhZFdyaXRlLkNyb3NzVGVuYW50QWNjZXNzIiwiQ2hhdC5SZWFkQmFzaWMuQWxsIiwiQ2FsbHMuQWNjZXNzTWVkaWEuQWxsIiwiQXBwbGljYXRpb24uUmVhZC5BbGwiLCJDbG91ZFBDLlJlYWRXcml0ZS5BbGwiLCJDaGF0TWVtYmVyLlJlYWQuQWxsIiwiQ3VzdG9tU2VjQXR0cmlidXRlQXNzaWdubWVudC5SZWFkV3JpdGUuQWxsIiwiUG9saWN5LlJlYWRXcml0ZS5BdXRob3JpemF0aW9uIiwiQ2FsbHMuSW5pdGlhdGUuQWxsIiwiUG9saWN5LlJlYWRXcml0ZS5GZWF0dXJlUm9sbG91dCIsIkNoYXQuQ3JlYXRlIiwiUG9saWN5LlJlYWRXcml0ZS5UcnVzdEZyYW1ld29yayJdLCJzdWIiOiJhNjljZTQxYS0xOTVmLTQ0YzUtOTRkOC0yMTVmMGYwMTg2MWEiLCJ0ZW5hbnRfcmVnaW9uX3Njb3BlIjoiRVUiLCJ0aWQiOiJiMGM5ZTRmOS1kNzJkLTQwNmYtYjI0Ny1lOGQ4NmM0YjQxNmEiLCJ1dGkiOiJzZnBMSHdfeE4wNmJxNTQyRFNPS0FBIiwidmVyIjoiMS4wIiwid2lkcyI6WyIwOTk3YTFkMC0wZDFkLTRhY2ItYjQwOC1kNWNhNzMxMjFlOTAiXSwieG1zX3RjZHQiOjE2NTMwMjEyMzR9.TBUVW-PEzMzFmmzDD7Iv-I2dfBoJSJPkYDR0KODqOwAfpY5-5f6rfvAIwpub_g4s0aZfZEEyWaJI_hK2ama7WT89y6qnK2BAJzvxklYCR-HrQkmsAgfZ02dVmhyRXMN9qUC7_CAk8fNKlTExzQIZlSjjtf-Lj1p95NLRKRA2bplLjkzW7cI3yqKNzoo42iTVaegRlGBK3Cak0AfcpqifpdEmpQGXP-5rFyegqK38KXGUMozURE2mSwGoUnwwcgcOkXc_x_TgBLvCkUrJC18gfvkVU0CKg6cjVHuYFvw-PUSyEnU2M8oNGru4he7bNB2gzvfsVhxrJuf8mD0YO_STHQ"
    )
}