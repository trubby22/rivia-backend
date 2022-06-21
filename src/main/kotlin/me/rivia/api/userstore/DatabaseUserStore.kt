package me.rivia.api.userstore

import me.rivia.api.database.Database
import me.rivia.api.teams.TeamsClient

class DatabaseUserStore(private val database: Database, private val applicationTeamsClient: TeamsClient) : UserStore {
    override fun getUser(tenantId: String, userId: String): User? {
        TODO("Not yet implemented")
    }
}
