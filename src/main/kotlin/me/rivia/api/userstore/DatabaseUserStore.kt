package me.rivia.api.userstore

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import me.rivia.api.database.Database
import me.rivia.api.database.Table
import me.rivia.api.database.getEntry
import me.rivia.api.database.putEntry
import me.rivia.api.teams.TeamsClient
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse


class DatabaseUserStore(private val database: Database, private val applicationTeamsClient: TeamsClient) : UserStore {
    override fun getUser(tenantId: String, userId: String): User? {
        // check if user is in database
        val userEntry = database.getEntry<User>(Table.USERS, tenantId + userId)
        if (userEntry == null) {
            // create api lambda to use with access/refresh token in client
            val apiCall = {accessToken : String ->
                val client = HttpClient.newHttpClient()
                // create a request
                val request = HttpRequest.newBuilder(
                    URI.create("https://graph.microsoft.com/v1.0/users/$userId")
                )
                    .header("Content-type", "application/json")
                    .header("Authorization", accessToken)
                    .build()
                val response = client.send(request, HttpResponse.BodyHandlers.ofString())
                val json : String? = response.body()
                json
            }
            val json = applicationTeamsClient.tokenOperation(tenantId, apiCall)
            val objectMapper = ObjectMapper()

            val jsonNode: JsonNode = objectMapper.readTree(json);

            if (jsonNode.get("error") != null) {
                return null
            }

            val firstName : String = jsonNode.get("givenName").asText()
            val surname : String = jsonNode.get("surname").asText()

            val newUser = User(tenantId + userId, firstName, surname)
            database.putEntry<User>(Table.USERS, newUser);

            return newUser
        } else {
            return userEntry;
        }

    }
}
