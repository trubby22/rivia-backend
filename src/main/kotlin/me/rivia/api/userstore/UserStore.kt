package me.rivia.api.userstore

data class User(val id: String, val name: String, val surname: String, val meetingIds: List<String>)

interface UserStore {
    fun getUser(tenantId: String, userId: String) : User?
}
