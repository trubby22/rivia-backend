package me.rivia.api.handlers

import me.rivia.api.Response
import me.rivia.api.ResponseError
import me.rivia.api.database.entry.Meeting
import me.rivia.api.database.*
import me.rivia.api.database.entry.ResponseParticipant
import me.rivia.api.database.entry.ResponsePresetQ
import me.rivia.api.database.entry.ResponseTenantUser


class PostReview : SubHandler {
    override fun handleRequest(
        url: List<String>,
        tenant: String,
        user: String?,
        jsonData: Map<String, Any?>,
        database: Database
    ): Response {
        val meetingId = url[1]
        if (!database.putEntry(Table.RESPONSETENANTUSERS, ResponseTenantUser(tenant, user!!, meetingId))) {
            return Response(ResponseError.REVIEWSUBMITTED)
        }
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
        val quality = jsonData["quality"]
        if (quality !is Float?) {
            return Response(ResponseError.WRONGENTRY)
        }
        val presetQIds = jsonData["presetQs"]
        if (presetQIds !is List<*>?) {
            return Response(ResponseError.WRONGENTRY)
        }
        if (presetQIds != null && presetQIds.checkListType<String>() == null) {
            return Response(ResponseError.WRONGENTRY)
        }

        val meetingEntry = database.updateEntry<Meeting>(Table.MEETINGS, meetingId) {
            if (quality != null) {
                it.qualities = it.qualities!! + listOf(quality)
            }
            it.feedbacks = it.feedbacks!! + listOf(feedback)
            it.responsesCount = it.responsesCount!! + 1
            it
        } ?: return Response(ResponseError.WRONGTENANTMEETING)

        if (presetQIds != null) {
                for (presetQId in meetingEntry.presetQIds!!) {
                    database.updateEntry<ResponsePresetQ>(Table.RESPONSEPRESETQS, presetQId) {
                        it.numSubmitted = it.numSubmitted!! + 1
                        it
                    }
                }

            for (presetQId in presetQIds) {
                database.updateEntry<ResponsePresetQ>(Table.RESPONSEPRESETQS, presetQId as String) {
                    it.numSubmitted = it.numSubmitted!! + 1
                    it
                }
            }
        }
        for (neededId in needed) {
            database.updateEntry<ResponseParticipant>(Table.RESPONSEPARTICIPANTS, neededId) {
                it.needed = it.needed!! + 1
                it
            }
        }
        for (notNeededId in notNeeded) {
            database.updateEntry<ResponseParticipant>(Table.RESPONSEPARTICIPANTS, notNeededId) {
                it.notNeeded = it.notNeeded!! + 1
                it
            }
        }
        for (preparedId in prepared) {
            database.updateEntry<ResponseParticipant>(Table.RESPONSEPARTICIPANTS, preparedId) {
                it.prepared = it.prepared!! + 1
                it
            }
        }
        for (notPreparedId in notPrepared) {
            database.updateEntry<ResponseParticipant>(Table.RESPONSEPARTICIPANTS, notPreparedId) {
                it.notPrepared = it.notPrepared!! + 1
                it
            }
        }
        return Response(null)
    }
}
