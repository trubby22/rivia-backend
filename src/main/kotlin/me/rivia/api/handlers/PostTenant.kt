package me.rivia.api.handlers

import me.rivia.api.Response
import me.rivia.api.database.Database

class PostTenant : SubHandler {
    override fun handleRequest(
        url: List<String>,
        tenant: String,
        user: String?,
        jsonData: Map<String, Any?>,
        database: Database
    ): Response {
        TODO("Not yet implemented")
        // If organization is present, update the token if needed and preset questions if needed (Tenants, updateEntry)
        // If organization is not present, set the token and preset questions if specified, or the default ones if not (Tenants, addEntry)
    }
}
