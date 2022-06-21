package me.rivia.api.userstore

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import jdk.incubator.jpackage.internal.IOUtils
import me.rivia.api.database.Database
import me.rivia.api.database.Table
import me.rivia.api.database.getEntry
import me.rivia.api.database.putEntry
import me.rivia.api.teams.TeamsClient
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.http.HttpExecuteRequest
import software.amazon.awssdk.http.SdkHttpMethod
import software.amazon.awssdk.http.SdkHttpRequest
import software.amazon.awssdk.protocols.jsoncore.JsonNode
import java.net.URLEncoder

data class UserResponse(
    @SerializedName("displayName") val displayName: String,
    @SerializedName("surname") val surname: String,
)



class DatabaseUserStore(private val database: Database, private val applicationTeamsClient: TeamsClient) : UserStore {
    override fun getUser(tenantId: String, userId: String): User? {
        // check if user is in database
        val userEntry = database.getEntry<User>(Table.USERS, tenantId + userId)
        if (userEntry == null) {
            // create api lambda to use with access/refresh token in client
            val apiCall = {accessToken : String ->
                val client = UrlConnectionHttpClient.builder().build()

                val request = client.prepareRequest(
                    HttpExecuteRequest.builder().request
                        (
                        SdkHttpRequest.builder().uri(
                            URI.create(
                                "https://graph.microsoft.com/v1.0/users/${userId}"
                            )
                        ).putHeader
                            ("Content-Type", "application/json")
                            .putHeader("Authorization", "Bearer $accessToken").method
                            (SdkHttpMethod.GET).build()
                    ).build()
                )

                val response = request.call()

                client.close()

                val inputStream = response.responseBody().get()
                val json = inputStream.toString()
                json
            }
            val json = applicationTeamsClient.tokenOperation(tenantId, apiCall)
            val userResponse = Gson().fromJson(
                json,
                UserResponse::class.java
            )
            // no meeting Ids when first adding user
            val newUser = User(tenantId + userId, userResponse.displayName, userResponse.surname, listOf())
            database.putEntry(Table.USERS, newUser);

            return newUser
        } else {
            return userEntry;
        }

    }
}
