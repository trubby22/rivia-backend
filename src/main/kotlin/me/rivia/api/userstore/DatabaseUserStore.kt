package me.rivia.api.userstore

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import me.rivia.api.database.Database
import me.rivia.api.database.Table
import me.rivia.api.database.entry.userId
import me.rivia.api.database.entry.User as DatabaseUser
import me.rivia.api.database.getEntry
import me.rivia.api.database.putEntry
import me.rivia.api.teams.TeamsClient
import software.amazon.awssdk.http.HttpExecuteRequest
import software.amazon.awssdk.http.SdkHttpMethod
import software.amazon.awssdk.http.SdkHttpRequest
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import java.net.URI
import kotlin.reflect.full.declaredMemberProperties


data class UserResponse(
    @SerializedName("givenName") val givenName: String,
    @SerializedName("surname") val surname: String,
)

class DatabaseUserStore(private val database: Database, private val applicationTeamsClient: TeamsClient) : UserStore {
    private val client = UrlConnectionHttpClient.builder().build()
    private val gson = Gson();

    override fun getUser(tenantId: String, userId: String): User? {
        val userEntry = database.getEntry<DatabaseUser>(Table.USERS, tenantId + userId)
        if (userEntry != null) {
            return User(
                userEntry.userId!!, userEntry.name!!, userEntry.surname!!, userEntry.meetingIds!!
            )
        }
        val json = applicationTeamsClient.tokenOperation(tenantId) { accessToken: String ->
            val response = client.prepareRequest(
                HttpExecuteRequest.builder().request
                    (
                    SdkHttpRequest.builder().uri(
                        URI.create(
                            "https://graph.microsoft.com/v1.0/users/${userId}?\$select=${UserResponse::class.declaredMemberProperties.joinToString(","){it.name}}"
                        )
                    ).putHeader
                        ("Content-Type", "application/json")
                        .putHeader("Authorization", "Bearer $accessToken").method
                            (SdkHttpMethod.GET).build()
                ).build()
            ).call()

            if (response.httpResponse().isSuccessful) response.responseBody().get().readAllBytes().toString() else null
        }

        val userResponse: UserResponse = gson.fromJson(json, UserResponse::class.java)
        val newUser = DatabaseUser(userId, userResponse.givenName, userResponse.surname, listOf())
        database.putEntry(Table.USERS, newUser);
        return User(
            newUser.userId!!, newUser.name!!, newUser.surname!!, newUser.meetingIds!!
        )
    }
}
fun main(args : Array<String>) {

}




