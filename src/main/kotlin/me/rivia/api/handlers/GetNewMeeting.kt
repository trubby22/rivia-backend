package me.rivia.api.handlers

import com.amazonaws.services.lambda.runtime.Context
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import aws.smithy.kotlin.runtime.time.Instant
import aws.smithy.kotlin.runtime.time.epochMilliseconds

import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.model.GetItemRequest
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.lambda.runtime.RequestHandler
import me.rivia.api.database.Table
import me.rivia.api.database.getAllEntries
import me.rivia.api.database.getEntry
import kotlin.system.exitProcess
import me.rivia.api.database.User as BackendUser

class GetNewMeeting {
    companion object {
        class ApiContext(var cookie: String?) {
            constructor() : this(null)
        }

        class HttpResponse(var participants: List<Participant>?) {
            val response_type: Int? = 3
        }
    }

    fun handle(
        input: ApiContext?,
        context: Context?
    ): HttpResponse {
        val users: List<BackendUser> = getAllEntries<BackendUser>(Table.USER)
        val participants = users.map {
            Participant(
                participant_id = it.userId,
                name = it.name,
                surname = it.surname,
                email = it.email,
            )
        }
        return HttpResponse(participants)
    }
}
