package me.rivia.api.handlers

import me.rivia.api.Response
import me.rivia.api.ResponseError
import me.rivia.api.database.Database
import me.rivia.api.database.Table
import me.rivia.api.database.entry.Tenant
import me.rivia.api.database.getEntry
import me.rivia.api.teams.TeamsClient
import me.rivia.api.userstore.UserStore
import me.rivia.api.websocket.WebsocketClient
import me.rivia.api.handlers.responses.PresetQ as ResponsePresetQ

class GetTenant : SubHandler {
    override fun handleRequest(
        url: List<String>,
        tenantId: String,
        userId: String?,
        jsonData: Map<String, Any?>,
        database: Database,
        userStore: UserStore,
        userAccessToken: TeamsClient,
        applicationAccessToken: TeamsClient,
        websocket: WebsocketClient
    ): Response {
        val tenantEntry = database.getEntry<Tenant>(Table.TENANTS, tenantId) ?: return Response(ResponseError.NOTENANT)
        val responsePresetQs = tenantEntry.presetQIds!!.map {ResponsePresetQ(database.getEntry(Table.PRESETQS, it) ?: throw Error("presetQ not found"))}
        return Response(responsePresetQs)
    }
}
