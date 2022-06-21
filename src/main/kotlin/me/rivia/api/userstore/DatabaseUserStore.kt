package me.rivia.api.userstore

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import me.rivia.api.database.Database
import me.rivia.api.database.Table
import me.rivia.api.database.getEntry
import me.rivia.api.database.putEntry
import me.rivia.api.teams.TeamsClient
import java.net.URI
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.http.HttpExecuteRequest
import software.amazon.awssdk.http.SdkHttpMethod
import software.amazon.awssdk.http.SdkHttpRequest

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

            // make API call
            val json = applicationTeamsClient.tokenOperation(tenantId, apiCall)
            val jsonObject : JsonObject = JsonParser().parse(json).asJsonObject

            // if id not found in microsoft database then return null
            if (jsonObject.getAsJsonObject("error").asString != null) {
                return null
            }
            val displayName : String = jsonObject.getAsJsonObject("displayName").asString;
            val surname : String = jsonObject.getAsJsonObject("surname").asString;

            // no meeting Ids when first adding user, will be updated by other code
            val newUser = User(tenantId + userId, displayName, surname, listOf())
            database.putEntry(Table.USERS, newUser);

            return newUser
        } else {
            // otherwise already stored in "cache" database so just return
            return userEntry;
        }

    }
}
