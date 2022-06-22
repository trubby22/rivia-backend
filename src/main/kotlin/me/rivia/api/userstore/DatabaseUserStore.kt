package me.rivia.api.userstore

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import me.rivia.api.database.Database
import me.rivia.api.database.Table
import me.rivia.api.database.entry.User
import me.rivia.api.database.getEntry
import me.rivia.api.database.putEntry
import me.rivia.api.teams.TeamsClient
import software.amazon.awssdk.http.HttpExecuteRequest
import software.amazon.awssdk.http.SdkHttpMethod
import software.amazon.awssdk.http.SdkHttpRequest
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import java.io.FileReader
import java.io.IOException
import java.net.URI


data class UserResponse(
    @SerializedName("@odata.context") val context : String,
    @SerializedName("displayName") val displayName: String,
    @SerializedName("surname") val surname: String,
)


class DatabaseUserStore(private val database: Database, private val applicationTeamsClient: TeamsClient) : UserStore {
    private val client = UrlConnectionHttpClient.builder().build()
    private val gson : Gson = Gson();

    override fun getUser(tenantId: String, userId: String): me.rivia.api.userstore.User? {
        // check if user is in database
        val userEntry : User? = database.getEntry<User>(Table.USERS, tenantId + userId)

        if (userEntry != null) {
            return me.rivia.api.userstore.User(
                userEntry.tenantIdUserId!!, userEntry.name!!, userEntry.surname!!, userEntry.meetingIds!!
            )
        }
        // make API call
        val json = applicationTeamsClient.tokenOperation(tenantId) { accessToken: String ->
            val response = client.prepareRequest(
                HttpExecuteRequest.builder().request
                    (
                    SdkHttpRequest.builder().uri(
                        URI.create(
                            "https://graph.microsoft.com/v1.0/users/${userId}?select=displayName,surname"
                        )
                    ).putHeader
                        ("Content-Type", "application/json")
                        .putHeader("Authorization", "Bearer $accessToken").method
                            (SdkHttpMethod.GET).build()
                ).build()
            ).call()

            if (!response.httpResponse().isSuccessful) {
                System.err.println("HTTP failure")
                client.close()
                "error"
            } else {
                client.close()
                val json = response.responseBody().get().readAllBytes().toString()
                System.err.println(json)
                json
            }
        }
        if (json == "error") {
            System.err.println("HTTP error or client not found");
            return null
        }
        val userResponse : UserResponse = gson.fromJson(json, UserResponse::class.java)


        // no meeting Ids when first adding user, will be updated by other code
        val newUser = User(tenantId + userId, userResponse.displayName, userResponse.surname, listOf())
        database.putEntry(Table.USERS, newUser);
        return me.rivia.api.userstore.User(
            newUser.tenantIdUserId!!, newUser.name!!, newUser.surname!!, newUser.meetingIds!!
        )

    }
}
fun main(args : Array<String>) {

}




