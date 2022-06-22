package me.rivia.api.handlers

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import me.rivia.api.Response
import me.rivia.api.ResponseError
import me.rivia.api.database.*
import me.rivia.api.database.entry.PresetQ
import me.rivia.api.database.entry.Tenant
import me.rivia.api.graphhttp.MicrosoftGraphAccessClient
import me.rivia.api.teams.TeamsClient
import me.rivia.api.userstore.UserStore
import me.rivia.api.websocket.WebsocketClient
import java.time.OffsetDateTime
import me.rivia.api.handlers.responses.PresetQ as ResponsePresetQ

class PostTenant : SubHandler {
    companion object {
        private data class SubscriptionResponse(
            @SerializedName("id") val id: String
        )
    }

    private val jsonConverter = Gson()

    override fun handleRequest(
        url: List<String>,
        tenantId: String?,
        userId: String?,
        jsonData: Map<String, Any?>,
        database: Database,
        userStore: UserStore,
        userAccessToken: TeamsClient,
        applicationAccessToken: TeamsClient,
        graphAccessClient: MicrosoftGraphAccessClient,
        websocket: WebsocketClient
    ): Response {
        // Extracting json data
        val userRefreshToken = jsonData["refreshToken"]
        if (userRefreshToken !is String?) {
            return Response(ResponseError.WRONGENTRY)
        }
        val presetQTexts = jsonData["presetQs"]
        if (presetQTexts !is List<*>?) {
            return Response(ResponseError.WRONGENTRY)
        }
        if (presetQTexts != null) {
            for (text in presetQTexts) {
                if (text !is String) {
                    return Response(ResponseError.WRONGENTRY)
                }
            }
        }

        // Generating presetQIds
        val defaultPresetQs = lazy {
            database.getAllEntries<PresetQ>(Table.PRESETQS).filter {
                it.isdefault!!
            }
        }
        val defaultPresetQIds = lazy {
            defaultPresetQs.value.map { it.presetQId!! }
        }
        val presetQs = presetQTexts?.let {
            lazy {
                presetQTexts.map {
                    lateinit var presetQ: PresetQ
                    do {
                        presetQ = PresetQ(generateUid(), it as String, false)
                    } while (!database.putEntry(Table.PRESETQS, presetQ))
                    presetQ
                }
            }
        }
        val presetQIds = presetQs?.let {
            lazy {
                presetQs.value.map { it.presetQId!! }
            }
        }

        val subscriptionId = createSubscription(
            graphAccessClient, applicationAccessToken, tenantId
        ).id

        // Inserting the tenant entry
        val tenantEntry: Tenant = if (userRefreshToken == null) {
            database.updateEntry(
                Table.TENANTS, tenantId!!
            ) { tenantEntry: Tenant ->
                tenantEntry.applicationAccessToken =
                    applicationAccessToken.refreshAccessToken(tenantId)
                tenantEntry.presetQIds =
                    presetQIds?.value ?: defaultPresetQIds.value
                tenantEntry
            } ?: return Response(ResponseError.NOTENANT)
        } else {
            database.updateEntryWithDefault(Table.TENANTS, {
                val applicationAccessToken =
                    applicationAccessToken.refreshAccessToken(
                        tenantId ?: throw Error(
                            "Tenant id null"
                        )
                    )
                val userAccessToken = userAccessToken.refreshAccessToken(
                    tenantId, userRefreshToken
                )
                Tenant(
                    tenantId,
                    subscriptionId,
                    applicationAccessToken,
                    userRefreshToken,
                    userAccessToken,
                    presetQIds?.value ?: defaultPresetQIds.value
                )
            }, { tenantEntry: Tenant ->
                tenantEntry.applicationAccessToken =
                    applicationAccessToken.refreshAccessToken(
                        tenantId ?: throw Error(
                            "Tenant id null"
                        )
                    )
                tenantEntry.userRefreshToken = userRefreshToken
                tenantEntry.userAccessToken =
                    userAccessToken.refreshAccessToken(
                        tenantId, userRefreshToken
                    )
                if (presetQIds != null) {
                    tenantEntry.presetQIds = presetQIds.value
                }
                tenantEntry
            })
        }

        val result = Response(tenantEntry.presetQIds!!.map {
            ResponsePresetQ(
                database.getEntry(Table.PRESETQS, it)
                    ?: throw Error("presetQ not present")
            )
        })
        return result
    }

    private fun createSubscription(
        graphAccessClient: MicrosoftGraphAccessClient,
        applicationAccessToken: TeamsClient,
        tenantId: String?
    ): SubscriptionResponse {
        val body = jsonConverter.toJson(
            PostSubscription.Companion.SubscriptionBody(
                changeType = PostSubscription.CHANGE_TYPE,
                notificationUrl = PostSubscription.NOTIFICATION_URL,
                resource = PostSubscription.RESOURCE,
                expirationDateTime = OffsetDateTime.now().plusMinutes(59),
                includeResourceData = true,
                encryptionCertificate = PostSubscription.CERTIFICATE,
                encryptionCertificateId = PostSubscription.CERTIFICATE_ID
            )
        )

        return applicationAccessToken.tokenOperation(tenantId!!) { token: String ->
            graphAccessClient.sendRequest(
                url = PostSubscription.SUBSCRIPTION_URL_BETA,
                headers = listOf(
                    "Content-Type" to "application/json",
                    "Authorization" to "Bearer $token"
                ),
                method = MicrosoftGraphAccessClient.Companion.HttpMethod.POST,
                body = body,
                clazz = SubscriptionResponse::class,
                queryArgs = listOf()
            ) ?: throw Error("Create subscription request failed")
        }
    }
}
