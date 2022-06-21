package me.rivia.api.handlers.responses

import me.rivia.api.database.entry.User as DatabaseUser

class User(val id: String, val name: String, val surname: String) {
    constructor(user: DatabaseUser) : this(user.userId!!, user.name!!, user.surname!!)
}
