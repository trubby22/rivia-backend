package me.rivia.api.database

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import kotlin.reflect.full.*
import kotlin.reflect.KClass

val dbClient: DynamoDbClient = DynamoDbClient.builder()
    .region(Region.EU_WEST_2)
    .build()

val dbEnhancedClient: DynamoDbEnhancedClient =
    DynamoDbEnhancedClient.builder().dynamoDbClient(dbClient).build()

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

data class Login(
    @get:DynamoDbPartitionKey
    var email: String? = null,
    var password: String? = null,
    var salt: String? = null,
    var user: String? = null
) : DbEntry {
    override fun primaryKeyName(): String = "email"
}

@DynamoDbBean
class Meeting(
    @get:DynamoDbPartitionKey
    var meetingId: String? = null,
    var title: String? = null,
    var participants: Set<String>? = null,
    var reviews: Set<String>? = null,
    var reviewedBy: Set<String>? = null,
    var startTime: Int? = null,
    var endTime: Int? = null,
) : DbEntry {
    override fun primaryKeyName(): String = "meetingId"
}

@DynamoDbBean
data class PresetQ(
    @get:DynamoDbPartitionKey
    var presetQId: String? = null,
    var text: String? = null
) : DbEntry {
    override fun primaryKeyName(): String = "presetQId"
}

@DynamoDbBean
class Review(
    @get:DynamoDbPartitionKey
    var reviewId: String? = null,
    var user: String? = null,
    var notNeeded: Set<String>? = null,
    var notPrepared: Set<String>? = null,
    var presetQs: Set<String>? = null,
    var quality: Float? = null
) : DbEntry {
    override fun primaryKeyName(): String = "reviewId"
}

@DynamoDbBean
data class Session(
    @get:DynamoDbPartitionKey
    var cookie: String? = null,
    var user: String? = null
) : DbEntry {
    override fun primaryKeyName(): String = "cookie"
}

@DynamoDbBean
data class User(
    @get:DynamoDbPartitionKey
    var userId: String? = null,
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

class FieldError(tableName: Table, field: String) :
    Error("'$field' field of the '$tableName' table not present")
