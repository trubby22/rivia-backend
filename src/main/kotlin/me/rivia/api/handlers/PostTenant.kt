package me.rivia.api.handlers

import me.rivia.api.Response
import me.rivia.api.ResponseError
import me.rivia.api.database.*
import me.rivia.api.database.entry.PresetQ
import me.rivia.api.database.entry.Tenant
import me.rivia.api.teams.TeamsClient
import me.rivia.api.userstore.UserStore
import me.rivia.api.websocket.WebsocketClient
import me.rivia.api.handlers.responses.PresetQ as ResponsePresetQ

class PostTenant : SubHandler {
    override fun handleRequest(
        url: List<String>,
        tenantId: String?,
        userId: String?,
        jsonData: Map<String, Any?>,
        database: Database,
        userStore: UserStore,
        userAccessToken: TeamsClient,
        applicationAccessToken: TeamsClient,
        websocket: WebsocketClient
    ): Response {
        // Extracting json data
        val applicationRefreshToken = jsonData["adminRefreshToken"]
        if (applicationRefreshToken !is String?) {
            return Response(ResponseError.WRONGENTRY)
        }
        val userRefreshToken = jsonData["userRefreshToken"]
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

        // Inserting the tenant entry
        val tenantEntry: Tenant = if (applicationRefreshToken == null || userRefreshToken == null) {
            database.updateEntry(
                Table.TENANTS, tenantId!!
            ) { tenantEntry: Tenant ->
                if (applicationRefreshToken != null) {
                    tenantEntry.applicationRefreshToken = applicationRefreshToken
                    tenantEntry.applicationAccessToken = applicationAccessToken.getAccessToken(tenantId)
                }
                if (userRefreshToken != null) {
                    tenantEntry.userRefreshToken = userRefreshToken
                    tenantEntry.userAccessToken = userAccessToken.getAccessToken(tenantId)
                }
                tenantEntry.presetQIds = presetQIds?.value ?: defaultPresetQIds.value
                tenantEntry
            } ?: return Response(ResponseError.NOTENANT)
        } else {
            database.updateEntryWithDefault(Table.TENANTS, {
                val applicationAccessToken = applicationAccessToken.getAccessToken(tenantId!!)
                val userAccessToken = userAccessToken.getAccessToken(tenantId)
                Tenant(
                    tenantId,
                    applicationRefreshToken,
                    applicationAccessToken,
                    userRefreshToken,
                    userAccessToken,
                    if (presetQIds != null) {
                        presetQIds.value
                    } else {
                        defaultPresetQIds.value
                    }
                )
            }, { tenantEntry: Tenant ->
                tenantEntry.applicationRefreshToken = applicationRefreshToken
                tenantEntry.applicationAccessToken = applicationAccessToken.getAccessToken(tenantId!!)
                tenantEntry.userRefreshToken = userRefreshToken
                tenantEntry.userAccessToken = userAccessToken.getAccessToken(tenantId)
                if (presetQIds != null) {
                    tenantEntry.presetQIds = presetQIds.value
                }
                tenantEntry
            })
        }

        return Response(tenantEntry.presetQIds!!.map {
            ResponsePresetQ(
                database.getEntry(Table.PRESETQS, it) ?: throw Error("presetQ not present")
            )
        })
    }
}
