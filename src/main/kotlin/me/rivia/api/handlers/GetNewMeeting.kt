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
import kotlin.system.exitProcess

class GetNewMeeting {
    companion object {
        class ApiContext(var cookie: String?) {
            constructor() : this(null)
        }

        class HttpResponse(var meetings: Array<Participant>?) {
            val response_type: Int? = 3
        }
    }

    fun handle(input: ApiContext?, context: Context?): HttpResponse {
        runBlocking {
            val tableNameVal: String = "User"
            val keyName: String = "UserID"
            val keyVal: String? = input!!.cookie;
            if (keyVal != null) {
                getSpecificItem(tableNameVal, keyName, keyVal)
            };
        }
        return HttpResponse(null)
    }

    suspend fun getSpecificItem(tableNameVal: String, keyName: String, keyVal: String) {

        val keyToGet = mutableMapOf<String, AttributeValue>()
        keyToGet[keyName] = AttributeValue.S(keyVal)

        val request = GetItemRequest {
            key = keyToGet
            tableName = tableNameVal
        }

        DynamoDbClient { region = "eu-west-2" }.use { ddb ->
            val returnedItem = ddb.getItem(request)
            val numbersMap = returnedItem.item
            numbersMap?.forEach { key1 ->
                println(key1.key)
                println(key1.value)
            }
        }
    }
}
