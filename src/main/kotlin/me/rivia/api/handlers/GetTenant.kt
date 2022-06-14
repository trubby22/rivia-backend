package me.rivia.api.handlers

import me.rivia.api.Response
import me.rivia.api.ResponseError
import me.rivia.api.database.Database
import me.rivia.api.database.Table
import me.rivia.api.database.entry.Tenant
import me.rivia.api.database.getEntry
import me.rivia.api.handlers.responses.PresetQ as ResponsePresetQ

class GetTenant : SubHandler {
    override fun handleRequest(
        url: List<String>,
        tenant: String,
        user: String?,
        jsonData: Map<String, Any?>,
        database: Database
    ): Response {
        val tenantEntry = database.getEntry<Tenant>(Table.TENANTS, tenant) ?: return Response(ResponseError.NOTENANT)
        val responsePresetQs = tenantEntry.presetQIds!!.map {ResponsePresetQ(database.getEntry(Table.PRESETQS, it) ?: throw Error("presetQ not found"))}
        return Response(responsePresetQs)
    }
}
