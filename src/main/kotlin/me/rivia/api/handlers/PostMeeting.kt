package me.rivia.api.handlers

import me.rivia.api.Response
import me.rivia.api.ResponseError
import me.rivia.api.database.entry.*
import me.rivia.api.database.entry.Meeting
import me.rivia.api.database.entry.ResponseParticipant
import me.rivia.api.database.entry.Tenant
import me.rivia.api.database.*

class PostMeeting : SubHandler {
    override fun handleRequest(
        url: List<String>,
        tenant: String,
        user: String?,
        jsonData: Map<String, Any?>,
        database: Database
    ): Response {
        val title = jsonData["title"] as? String ?: return Response(ResponseError.WRONGENTRY)
        val startTime = jsonData["startTime"] as? Int ?: return Response(ResponseError.WRONGENTRY)
        val endTime = jsonData["endTime"] as? Int ?: return Response(ResponseError.WRONGENTRY)
        val organizerData =
            jsonData["organizer"] as? Map<*, *> ?: return Response(ResponseError.WRONGENTRY)
        val organizerName =
            organizerData["name"] as? String ?: return Response(ResponseError.WRONGENTRY)
        val organizerSurname =
            organizerData["surname"] as? String ?: return Response(ResponseError.WRONGENTRY)
        val participantsData =
            (jsonData["participants"] as? List<*>)?.checkListType<Map<*, *>>() ?: return Response(ResponseError.WRONGENTRY)
        val participantsNamesSurnames = participantsData.map {
            val participantName =
                it["name"] as? String ?: return Response(ResponseError.WRONGENTRY)
            val participantSurname =
                it["surname"] as? String ?: return Response(ResponseError.WRONGENTRY)
            Pair(participantName, participantSurname)
        }

        val tenantEntry = database.getEntry<Tenant>(Table.TENANTS, tenant) ?: return Response(
            ResponseError.NOTENANT
        )

        lateinit var organizer: Participant
        do {
            organizer = Participant(generateUid(), organizerName, organizerSurname)
        } while (!database.putEntry(Table.PARTICIPANTS, organizer))

        val participants = participantsNamesSurnames.map { (name: String, surname: String) ->
            lateinit var participant: Participant
            do {
                participant = Participant(generateUid(), name, surname)
            } while (!database.putEntry(Table.PARTICIPANTS, participant))
            participant
        } + listOf(organizer)


        lateinit var meeting: Meeting
        do {
            meeting = Meeting(
                generateUid(),
                tenantEntry.presetQIds!!,
                organizer.participantId!!,
                participants.map { it.participantId!! },
                title,
                startTime,
                endTime,
                0,
                listOf(),
                listOf()
            )
        } while (!database.putEntry(Table.MEETINGS, meeting))

        for (participant in participants) {
            if (!database.putEntry(
                    Table.RESPONSEPARTICIPANTS, ResponseParticipant(
                        participant.participantId!!, meeting.meetingId!!, 0, 0, 0, 0
                    )
                )
            ) {
                throw Error("Entry already present")
            }
        }

        for (presetQId in tenantEntry.presetQIds!!) {
            if (!database.putEntry(
                    Table.RESPONSEPRESETQS, ResponsePresetQ(presetQId, meeting.meetingId!!, 0, 0)
                )
            ) {
                throw Error("Entry already present")
            }
        }

        if (!database.putEntry(Table.TENANTMEETINGS, TenantMeeting(tenant, meeting.meetingId!!))) {
            throw Error("Entry already present")
        }

        if (database.updateEntry<Tenant>(Table.TENANTS, tenant) {
                it.meetingIds = it.meetingIds!! + listOf(meeting.meetingId!!)
                it
            } == null) {
            throw Error("Tenant removed")
        }
        return Response(meeting.meetingId)

        // Meetings
        // TenantMeetings
        // ResponseParticipants
        // ResponsePresetQs
        // TenantMeetings
        // Tenant
    }
}
