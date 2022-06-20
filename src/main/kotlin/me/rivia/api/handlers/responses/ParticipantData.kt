package me.rivia.api.handlers.responses

data class ParticipantData(var participant: Participant, var needed: Int, var notNeeded: Int, var prepared: Int, var notPrepared: Int)
