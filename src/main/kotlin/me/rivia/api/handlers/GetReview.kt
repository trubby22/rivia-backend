package me.rivia.api.handlers

import me.rivia.api.Response
import me.rivia.api.database.Database
import me.rivia.api.database.Table
import me.rivia.api.database.entry.ResponseTenantUser
import me.rivia.api.database.getEntry

class GetReview : SubHandler {
    override fun handleRequest(
        url: List<String>,
        tenant: String,
        user: String?,
        jsonData: Map<String, Any?>,
        database: Database
    ): Response {
        val rtuKey : String = "tenant" + "user" + url[1]
        val responseTenantUserEntry = database.getEntry<ResponseTenantUser>(Table.RESPONSETENANTUSERS, rtuKey);
        val result = responseTenantUserEntry == null;
        return Response(!result);
    }
}
