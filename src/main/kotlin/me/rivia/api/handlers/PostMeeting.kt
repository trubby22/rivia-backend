package me.rivia.api.handlers

import me.rivia.api.Response
import me.rivia.api.ResponseError
import me.rivia.api.database.entry.*
import me.rivia.api.database.entry.Meeting
import me.rivia.api.database.entry.Tenant
import me.rivia.api.database.*
import me.rivia.api.graphhttp.MicrosoftGraphAccessClient
import me.rivia.api.handlers.responses.MeetingId
import me.rivia.api.teams.TeamsClient
import me.rivia.api.userstore.UserStore
import me.rivia.api.websocket.WebsocketClient

class PostMeeting : SubHandler {
    override fun handleRequest(
        url: List<String>,
        tenantId: String?,
        _userId: String?,
        validationToken: String,
        jsonData: Map<String, Any?>,
        database: Database,
        userStore: UserStore,
        userAccessToken: TeamsClient,
        applicationAccessToken: TeamsClient,
        graphAccessClient: MicrosoftGraphAccessClient,
        websocket: WebsocketClient
    ): Response {
        val title = jsonData["title"] as? String ?: return Response(ResponseError.WRONGENTRY)
        val startTime = jsonData["startTime"] as? Int ?: return Response(ResponseError.WRONGENTRY)
        val endTime = jsonData["endTime"] as? Int ?: return Response(ResponseError.WRONGENTRY)
        val organizerId =
            jsonData["organizerId"] as? String ?: return Response(ResponseError.WRONGENTRY)
        val userIds = (jsonData["userIds"] as? List<*>)?.checkListType<String>() ?: return Response(
            ResponseError.WRONGENTRY
        )

        val tenantEntry = database.getEntry<Tenant>(Table.TENANTS, tenantId!!) ?: return Response(
            ResponseError.NOTENANT
        )

        lateinit var meetingEntry: Meeting
        do {
            meetingEntry = Meeting(
                generateUid(),
                tenantId,
                organizerId,
                userIds,
                title,
                startTime,
                endTime,
                0,
                tenantEntry.presetQIds!!,
                listOf(),
                listOf()
            )
        } while (!database.putEntry(Table.MEETINGS, meetingEntry))

        for (presetQId in tenantEntry.presetQIds!!) {
            if (!database.putEntry(
                    Table.RESPONSEPRESETQS,
                    ResponsePresetQ(presetQId, meetingEntry.meetingId!!, 0, 0)
                )
            ) {
                throw Error("Entry already present")
            }
        }
        for (userId in userIds) {
            userStore.getUser(tenantId, userId)
            if (database.updateEntry<User>(Table.USERS, User.constructKey(tenantId, userId)) {
                    it.meetingIds = it.meetingIds!! + listOf(meetingEntry.meetingId!!)
                    it
                } == null) {
                throw Error("User removed")
            }
            if (!database.putEntry(
                    Table.RESPONSEDATAUSERS,
                    ResponseDataUser(tenantId, userId, meetingEntry.meetingId!!, 0, 0, 0, 0)
                )
            ) {
                throw Error("Entry already present")
            }
        }
        websocket.sendEvent(
            { otherTenantId, otherUserId -> otherTenantId == tenantId && otherUserId in meetingEntry.userIds!! },
            MeetingId.fetch(database, userStore, meetingEntry.meetingId!!)!!.second
        )
        return Response(meetingEntry.meetingId)
    }
}
