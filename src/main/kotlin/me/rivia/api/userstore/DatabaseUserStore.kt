package me.rivia.api.userstore

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import me.rivia.api.database.Database
import me.rivia.api.database.Table
import me.rivia.api.database.entry.userId
import me.rivia.api.database.entry.User as DatabaseUser
import me.rivia.api.database.getEntry
import me.rivia.api.database.putEntry
import me.rivia.api.graphhttp.*
import me.rivia.api.graphhttp.MicrosoftGraphAccessClient.Companion.HttpMethod
import me.rivia.api.teams.TeamsClient
import kotlin.reflect.full.declaredMemberProperties


data class UserResponse(
    @SerializedName("givenName") val givenName: String,
    @SerializedName("surname") val surname: String,
)

class DatabaseUserStore(
    private val database: Database,
    private val microsoftGraphAccessClient: MicrosoftGraphAccessClient,
    private val applicationTeamsClient: TeamsClient
) : UserStore {
    override fun getUser(tenantId: String, userId: String): User? {
        val userEntry = database.getEntry<DatabaseUser>(Table.USERS, DatabaseUser.constructKey(tenantId, userId))
        if (userEntry != null) {
            return User(
                userEntry.userId!!, userEntry.name!!, userEntry.surname!!, userEntry.meetingIds!!
            )
        }
        val userResponse = applicationTeamsClient.tokenOperation(tenantId) { accessToken: String ->
            microsoftGraphAccessClient.sendRequest<UserResponse>(
                "https://graph.microsoft.com/v1.0/users/${userId}",
                listOf("\$select" to UserResponse::class.declaredMemberProperties.map { it.name }),
                HttpMethod.GET,
                listOf("Authorization" to "Bearer ${accessToken}",
                    "Accept" to "application/json"),
                null
            )
        }
        val newUser = DatabaseUser(tenantId, userId, userResponse.givenName, userResponse.surname, listOf())
        database.putEntry(Table.USERS, newUser);
        return User(
            newUser.userId!!, newUser.name!!, newUser.surname!!, newUser.meetingIds!!
        )
    }
}




