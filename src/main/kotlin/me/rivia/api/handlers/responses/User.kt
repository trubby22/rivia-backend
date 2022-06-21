package me.rivia.api.handlers.responses

import me.rivia.api.userstore.User as StoreUser

class User(val id: String, val name: String, val surname: String, val meetingIds: List<String>) {
    constructor(user: StoreUser) : this(user.id, user.name, user.surname, user.meetingIds)
}
