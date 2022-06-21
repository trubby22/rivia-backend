package me.rivia.api.handlers

import me.rivia.api.Response
import me.rivia.api.ResponseError
import me.rivia.api.database.*
import me.rivia.api.database.entry.*
import me.rivia.api.handlers.responses.MeetingId
import me.rivia.api.teams.TeamsClient
import me.rivia.api.userstore.UserStore
import me.rivia.api.websocket.WebsocketClient


class PostReview : SubHandler {
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
        // Extracting json data
        val meetingId = url[1]
        val needed = (jsonData["needed"] as? List<*>)?.checkListType<String>() ?: return Response(
            ResponseError.WRONGENTRY
        )
        val notNeeded =
            (jsonData["notNeeded"] as? List<*>)?.checkListType<String>() ?: return Response(
                ResponseError.WRONGENTRY
            )
        val prepared =
            (jsonData["prepared"] as? List<*>)?.checkListType<String>() ?: return Response(
                ResponseError.WRONGENTRY
            )
        val notPrepared =
            (jsonData["notPrepared"] as? List<*>)?.checkListType<String>() ?: return Response(
                ResponseError.WRONGENTRY
            )
        val feedback = jsonData["feedback"] as? String ?: return Response(ResponseError.WRONGENTRY)
        var quality = jsonData["quality"]
        if (quality !is Double? && quality !is Int?) {
            return Response(ResponseError.WRONGENTRY)
        }
        if (quality is Int?) {
            quality = quality?.toDouble()
        }
        val presetQIds = jsonData["presetQs"]
        if (presetQIds !is List<*>?) {
            return Response(ResponseError.WRONGENTRY)
        }
        if (presetQIds != null && presetQIds.checkListType<String>() == null) {
            return Response(ResponseError.WRONGENTRY)
        }

        // Inserting the data
        if (!database.putEntry(
                Table.RESPONSESUBMISSIONS, ResponseSubmission(tenantId, userId!!, meetingId)
            )
        ) {
            return Response(ResponseError.REVIEWSUBMITTED)
        }

        val meetingEntry = database.updateEntry<Meeting>(Table.MEETINGS, meetingId) {
            if (quality != null) {
                it.qualities = it.qualities!! + listOf(quality as Double)
            }
            if (feedback != "") {
                it.feedbacks = it.feedbacks!! + listOf(feedback)
            }
            it.responsesCount = it.responsesCount!! + 1
            it
        } ?: return Response(ResponseError.WRONGTENANTMEETING)

        if (presetQIds != null) {
            for (presetQId in meetingEntry.presetQIds!!) {
                database.updateEntry<ResponsePresetQ>(
                    Table.RESPONSEPRESETQS,
                    ResponsePresetQ(presetQId, meetingId, null, null).presetQIdMeetingId!!
                ) {
                    it.numSelected = it.numSelected!! + 1
                    it
                } ?: throw Error("ResponsePresetQ not present")
            }

            for (presetQId in presetQIds) {
                database.updateEntry<ResponsePresetQ>(
                    Table.RESPONSEPRESETQS,
                    ResponsePresetQ(presetQId as String, meetingId, null, null).presetQIdMeetingId!!
                ) {
                    it.numSubmitted = it.numSubmitted!! + 1
                    it
                } ?: throw Error("ResponsePresetQ not present")
            }
        }
        for (neededId in needed) {
            database.updateEntry<ResponseDataUser>(
                Table.RESPONSEDATAUSERS, ResponseDataUser.constructKey(tenantId, userId, meetingId)
            ) {
                it.needed = it.needed!! + 1
                it
            } ?: throw Error("ResponseParticipant not present")
        }
        for (notNeededId in notNeeded) {
            database.updateEntry<ResponseDataUser>(
                Table.RESPONSEDATAUSERS, ResponseDataUser.constructKey(tenantId, userId, meetingId)
            ) {
                it.notNeeded = it.notNeeded!! + 1
                it
            } ?: throw Error("ResponseParticipant not present")
        }
        for (preparedId in prepared) {
            database.updateEntry<ResponseDataUser>(
                Table.RESPONSEDATAUSERS, ResponseDataUser.constructKey(tenantId, userId, meetingId)
            ) {
                it.prepared = it.prepared!! + 1
                it
            } ?: throw Error("ResponseParticipant not present")
        }
        for (notPreparedId in notPrepared) {
            database.updateEntry<ResponseDataUser>(
                Table.RESPONSEDATAUSERS, ResponseDataUser.constructKey(tenantId, userId, meetingId)
            ) {
                it.notPrepared = it.notPrepared!! + 1
                it
            } ?: throw Error("ResponseParticipant not present")
        }
        websocket.sendEvent({_, _ -> true}, MeetingId.fetch(database, userStore, meetingId)!!.second)
        return Response(null)
    }
}
