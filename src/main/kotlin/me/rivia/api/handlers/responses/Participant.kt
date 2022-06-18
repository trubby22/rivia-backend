package me.rivia.api.handlers.responses

import me.rivia.api.database.entry.Participant as DatabaseParticipant

class Participant(val id: String, val name: String, val surname: String) {
    constructor(participant: DatabaseParticipant) : this(participant.participantId!!, participant.name!!, participant.surname!!)
}
