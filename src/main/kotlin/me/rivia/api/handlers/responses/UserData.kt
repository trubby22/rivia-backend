package me.rivia.api.handlers.responses

data class UserData(var user: User, var needed: Int, var notNeeded: Int, var prepared: Int, var notPrepared: Int)
