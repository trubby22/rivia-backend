package me.rivia.api.database

import software.amazon.awssdk.enhanced.dynamodb.*
import software.amazon.awssdk.enhanced.dynamodb.internal.EnhancedClientUtils.createKeyFromItem
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties

class DynamoDatabase(region: Region = Region.EU_WEST_2) : Database {
    private companion object {
        /**
         * Gets the name of the partition key
         */
        fun <EntryType> DynamoDbTable<EntryType>.partitionKeyName() =
            this.tableSchema().tableMetadata().primaryPartitionKey()

        /**
         * Gets the value of the partition key in the entry
         */
        fun <EntryType : Any> getPartitionKeyValue(
            table: DynamoDbTable<EntryType>, entry: EntryType
        ): AttributeValue = createKeyFromItem(
            entry, table.tableSchema(), table.partitionKeyName()
        ).partitionKeyValue()

        fun numberToText(x: Int): String {
            var xChanging = x;
            var out = ""
            do {
                out += (xChanging + 'a'.code).toChar()
                xChanging /= ('z'.code - 'a'.code + 1)
            } while(xChanging > 0)
            return out
        }

        /**
         * Checks if any class fields in the specified value are null
         */
        fun <T : Any> fieldNullCheck(value: T, errorMessage: String, clazz: KClass<T>): T {
            for (member in clazz.declaredMemberProperties) {
                if (member.get(value) == null) {
                    throw Error(errorMessage)
                }
            }
            return value
        }
    }

    private val httpClient = UrlConnectionHttpClient.builder().build()
    private val dbClient = DynamoDbClient.builder().region(region).httpClient(httpClient).build()
    private val dbEnhancedClient = DynamoDbEnhancedClient.builder().dynamoDbClient(dbClient).build()

    /**
     * Fetches the entry with the specified key from DynamoDB
     */
    override fun <EntryType : Any> getEntry(
        table: Table, keyValue: String, clazz: KClass<EntryType>
    ): EntryType? {
        val dynamoTable =
            dbEnhancedClient.table(table.toString(), TableSchema.fromClass(clazz.java))
        val entry =
            dynamoTable.getItem(Key.builder().partitionValue(keyValue).build()) ?: return null
        // Check for null entries
        return fieldNullCheck(entry, "${entry} from '$table' has a nulled component", clazz)
    }

    override fun <EntryType : Any> getAllEntries(
        table: Table, clazz: KClass<EntryType>
    ): List<EntryType> {
        val dynamoTable = dbEnhancedClient.table(table.toString(), TableSchema.fromBean(clazz.java))
        val entries = dynamoTable.scan().items().toList()

        for (entry in entries) {
            fieldNullCheck(entry, "$entry from '$table' has a nulled component", clazz)
        }
        return entries
    }

    private fun <EntryType : Any> putRequest(
        table: DynamoDbTable<EntryType>, entry: EntryType, clazz: KClass<EntryType>
    ): Boolean {
        val noEntryExpression =
            Expression.builder().expression("attribute_not_exists(#primaryKeyName)")
                .putExpressionName("#primaryKeyName", table.partitionKeyName()).build()
        val request = PutItemEnhancedRequest.builder(clazz.java).item(entry)
            .conditionExpression(noEntryExpression).build()
        try {
            table.putItem(request)
        } catch (e: ConditionalCheckFailedException) {
            return false
        }
        return true
    }

    private fun <EntryType : Any> updateRequest(
        table: DynamoDbTable<EntryType>,
        entry: EntryType,
        update: (EntryType) -> EntryType,
        clazz: KClass<EntryType>
    ): EntryType? {
        val sameEntryExpression = table.tableSchema().attributeNames().map { attributeName ->
            val attributeValue = table.tableSchema().attributeValue(entry, attributeName)
            Expression.builder().expression("#$attributeName = :$attributeName")
                    .putExpressionName("#$attributeName", attributeName)
                    .putExpressionValue(
                        ":$attributeName",
                        attributeValue
                    ).build()
        }.reduce(Expression::and)

        val newEntry = update(entry)

        val request = PutItemEnhancedRequest.builder(clazz.java).item(newEntry)
            .conditionExpression(sameEntryExpression).build()
        try {
            table.putItem(request)
        } catch (e: ConditionalCheckFailedException) {
            return null
        }
        return newEntry
    }

    override fun <EntryType : Any> updateEntryWithDefault(
        table: Table,
        default: () -> EntryType,
        update: (EntryType) -> EntryType,
        clazz: KClass<EntryType>
    ): EntryType {
        val dynamoTable = dbEnhancedClient.table(
            table.toString(), TableSchema.fromClass(clazz.java)
        )
        lateinit var defaultEntry: EntryType
        var entry: EntryType?
        var newEntry: EntryType? = null
        do {
            defaultEntry = default()
            entry = dynamoTable.getItem(defaultEntry)
        } while (!if (entry == null) {
                putRequest(dynamoTable, defaultEntry, clazz)
            } else {
                newEntry = updateRequest(dynamoTable, entry, update, clazz)
                newEntry != null
            }
        )
        return if (entry == null) defaultEntry else newEntry!!
    }

    override fun <EntryType : Any> updateEntry(
        table: Table, keyValue: String, update: (EntryType) -> EntryType, clazz: KClass<EntryType>
    ): EntryType? {
        val dynamoTable = dbEnhancedClient.table(
            table.toString(), TableSchema.fromClass(clazz.java)
        )
        lateinit var entry: EntryType
        var newEntry: EntryType?
        do {
            entry =
                dynamoTable.getItem(Key.builder().partitionValue(keyValue).build()) ?: return null
        } while (!run {
                newEntry = updateRequest(dynamoTable, entry, update, clazz)
                newEntry != null
            })
        return newEntry
    }

    override fun <EntryType : Any> putEntry(
        table: Table, entry: EntryType, clazz: KClass<EntryType>
    ): Boolean = putRequest(
        dbEnhancedClient.table(table.toString(), TableSchema.fromBean(clazz.java)), entry, clazz
    )

    override fun <EntryType : Any> deleteEntry(
        table: Table, keyValue: String, clazz: KClass<EntryType>
    ): EntryType? {
        val dynamoTable = dbEnhancedClient.table(table.toString(), TableSchema.fromBean(clazz.java))
        val entry : EntryType? = dynamoTable.deleteItem(Key.builder().partitionValue(keyValue).build())
        return entry?.apply {fieldNullCheck(entry, "$entry from '$table' has a nulled component", clazz)}
    }
}

