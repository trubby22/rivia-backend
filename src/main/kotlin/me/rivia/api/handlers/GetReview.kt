package me.rivia.api.handlers

import me.rivia.api.Response
import me.rivia.api.database.Database
import me.rivia.api.database.Table
import me.rivia.api.database.entry.ResponseSubmission
import me.rivia.api.database.getEntry
import me.rivia.api.teams.TeamsClient
import me.rivia.api.userstore.UserStore
import me.rivia.api.websocket.WebsocketClient

class GetReview : SubHandler {
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
        val meetingId = url[1]
        val responseSubmissionEntry = database.getEntry<ResponseSubmission>(Table.RESPONSESUBMISSIONS, ResponseSubmission.constructKey(tenantId!!, userId!!, meetingId))
        return Response(responseSubmissionEntry != null)
    }
}
