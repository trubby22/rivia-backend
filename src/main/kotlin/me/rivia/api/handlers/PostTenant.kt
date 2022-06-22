package me.rivia.api.handlers

import me.rivia.api.Response
import me.rivia.api.ResponseError
import me.rivia.api.database.*
import me.rivia.api.database.entry.PresetQ
import me.rivia.api.database.entry.Tenant
import me.rivia.api.graphhttp.MicrosoftGraphAccessClient
import me.rivia.api.teams.TeamsClient
import me.rivia.api.userstore.UserStore
import me.rivia.api.websocket.WebsocketClient
import me.rivia.api.handlers.responses.PresetQ as ResponsePresetQ

class PostTenant : SubHandler {
    override fun handleRequest(
        url: List<String>,
        tenantId: String?,
        userId: String?,
        validationToken: String,
        jsonData: Map<String, Any?>,
        database: Database,
        userStore: UserStore,
        userAccessToken: TeamsClient,
        applicationAccessToken: TeamsClient,
        graphAccessClient: MicrosoftGraphAccessClient,
        websocket: WebsocketClient
    ): Response {
        val presetQTexts = jsonData["presetQs"]
        if (presetQTexts !is List<*>?) {
            return Response(ResponseError.WRONGENTRY)
        }
        val presetQs = if (presetQTexts != null) {
            presetQTexts.checkListType<String>() ?: return Response(ResponseError.WRONGENTRY)
            presetQTexts.map {
                lateinit var presetQ: PresetQ
                do {
                    presetQ = PresetQ(generateUid(), it as String, false)
                } while (!database.putEntry(Table.PRESETQS, presetQ))
                presetQ
            }
        } else {
            database.getAllEntries<PresetQ>(Table.PRESETQS).filter {
                it.isdefault!!
            }
        }
        val presetQIds = presetQs.map { it.presetQId!! }

        database.updateEntry(
            Table.TENANTS, tenantId!!
        ) { tenantEntry: Tenant ->
            tenantEntry.presetQIds = presetQIds
            tenantEntry
        } ?: return Response(ResponseError.NOTENANT)

        return Response(presetQIds.map {
            ResponsePresetQ(
                database.getEntry(Table.PRESETQS, it) ?: throw Error("presetQ not present")
            )
        })
    }
}
