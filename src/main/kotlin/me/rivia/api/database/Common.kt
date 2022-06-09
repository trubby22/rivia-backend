package me.rivia.api.database

import me.rivia.api.handlers.Uid
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import kotlin.reflect.full.*
import kotlin.reflect.KClass
import java.util.*

val httpClient = UrlConnectionHttpClient.builder().build()

var dbClient: DynamoDbClient? = null

var dbEnhancedClient: DynamoDbEnhancedClient? = null

fun initDb() {
    dbClient = DynamoDbClient.builder()
        .region(Region.EU_WEST_2).httpClient(httpClient)
        .build()
    dbEnhancedClient = DynamoDbEnhancedClient.builder().dynamoDbClient(dbClient).build()
    dbClient!!.describeLimits()
}

interface DbEntry {
    fun primaryKeyName(): String
}

enum class Table(val tableName: String) {
    LOGIN("Login"),
    MEETING("Meeting"),
    PRESETQS("PresetQs"),
    REVIEW("Review"),
    SESSION("Session"),
    USER("User");

    override fun toString(): String {
        return tableName
    }
}

const val SINGLE_BATCH_LIMIT = 100

@DynamoDbBean
data class Login(
    @get:DynamoDbPartitionKey
    var email: String? = null,
    var password: String? = null,
    var salt: String? = null,
    var user: Uid? = null
) : DbEntry {
    override fun primaryKeyName(): String = "email"
}

@DynamoDbBean
class Meeting(
    @get:DynamoDbPartitionKey
    var meetingId: Uid? = null,
    var title: String? = null,
    var participants: List<Uid>? = null,
    var reviews: List<Uid>? = null,
    var reviewedBy: List<Uid>? = null,
    var startTime: Int? = null,
    var endTime: Int? = null,
) : DbEntry {
    override fun primaryKeyName(): String = "meetingId"

}

@DynamoDbBean
data class PresetQ(
    @get:DynamoDbPartitionKey
    var presetQId: Uid? = null,
    var text: String? = null
) : DbEntry {
    override fun primaryKeyName(): String = "presetQId"
}

@DynamoDbBean
class Review(
    @get:DynamoDbPartitionKey
    var reviewId: Uid? = null,
    var user: Uid? = null,
    var notNeeded: List<Uid>? = null,
    var notPrepared: List<Uid>? = null,
    var presetQs: List<Uid>? = null,
    var quality: Float? = null
) : DbEntry {
    override fun primaryKeyName(): String = "reviewId"
}

@DynamoDbBean
data class Session(
    @get:DynamoDbPartitionKey
    var cookie: Uid? = null,
    var user: Uid? = null
) : DbEntry {
    override fun primaryKeyName(): String = "cookie"
}

@DynamoDbBean
data class User(
    @get:DynamoDbPartitionKey
    var userId: Uid? = null,
    var email: String? = null,
    var name: String? = null,
    var surname: String? = null,
) : DbEntry {
    override fun primaryKeyName(): String = "userId"
}

inline fun <reified T : Any> fieldNullCheck(value: T, errorMessage: String): T {
    for (member in (value::class as KClass<T>).declaredMemberProperties) {
        if (member.get(value) == null) {
            throw Error(errorMessage)
        }
    }
    return value
}

inline fun <reified T : Any> entryNullCheck(value: T, table: Table): T =
    fieldNullCheck(value, "entry from '$table' has a nulled component")

inline fun <reified T : Any, C : Iterable<T>> entriesNullCheck(
    values: C,
    table: Table
): C {
    for (value in values) {
        entryNullCheck(value, table)
    }
    return values
}

class FieldError(tableName: Table, field: String) :
    Error("'$field' field of the '$tableName' table not present")

fun generateId(): String {
    return UUID.randomUUID().toString()
}
