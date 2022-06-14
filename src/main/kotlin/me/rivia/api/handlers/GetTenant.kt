package me.rivia.api.handlers

import me.rivia.api.Response
import me.rivia.api.database.Database

class GetTenant : SubHandler {
    override fun handleRequest(
        url: List<String>,
        tenant: String,
        user: String?,
        jsonData: Map<String, Any?>,
        database: Database
    ): Response {
        TODO("Not yet implemented")
        // If implemented, check if the user is in that tenant (Microsoft Graph)
        // Get the tenant and its preset questions (Tenants, getEntry)
    }
}
