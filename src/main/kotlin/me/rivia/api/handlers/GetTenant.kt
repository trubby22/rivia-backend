package me.rivia.api.handlers

import me.rivia.api.Response
import me.rivia.api.ResponseError
import me.rivia.api.database.Database
import me.rivia.api.database.Table
import me.rivia.api.database.entry.PresetQ
import me.rivia.api.database.entry.Tenant
import me.rivia.api.database.*
import me.rivia.api.handlers.responses.PresetQ as ResponsePresetQ

class GetTenant : SubHandler {
    override fun handleRequest(
        url: List<String>,
        tenant: String,
        user: String?,
        jsonData: Map<String, Any?>,
        database: Database
    ): Response {
        TODO("Not implemented yet")
}
