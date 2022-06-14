package me.rivia.api.handlers

import me.rivia.api.Response
import me.rivia.api.ResponseError
import me.rivia.api.database.Database
import me.rivia.api.database.Table
import me.rivia.api.database.entry.Tenant
import me.rivia.api.database.getEntry

class GetMeetings : SubHandler {
    override fun handleRequest(
        url: List<String>,
        tenant: String,
        user: String?,
        jsonData: Map<String, Any?>,
        database: Database
    ): Response {
        val tenantEntry = database.getEntry<Tenant>(Table.TENANTS, tenant) ?: return Response(
            ResponseError.NOTENANT)
        return Response(tenantEntry.meetingIds)
    }
}
