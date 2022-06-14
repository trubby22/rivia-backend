package me.rivia.api.database

import software.amazon.awssdk.enhanced.dynamodb.*
import software.amazon.awssdk.enhanced.dynamodb.internal.EnhancedClientUtils.createKeyFromItem
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException
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
    private val dbClient =
        DynamoDbClient.builder().region(region).httpClient(httpClient).build()
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
        return fieldNullCheck(entry, "entry from '$table' has a nulled component", clazz)
    }

    override fun <EntryType : Any> getAllEntries(
        table: Table,
        clazz: KClass<EntryType>
    ): List<EntryType> {
        val dynamoTable = dbEnhancedClient.table(table.toString(), TableSchema.fromBean(clazz.java))
        val entries = dynamoTable.scan().items().toList()
        for (entry in entries) {
            fieldNullCheck(entry, "entry from '$table' has a nulled component", clazz)
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
        oldEntry: EntryType,
        newEntry: EntryType,
        clazz: KClass<EntryType>
    ): Boolean {
        val sameEntryExpression = table.tableSchema().attributeNames().map { attributeName ->
            Expression.builder()
                .expression("#AttributeName$attributeName = :AttributeValue$attributeName")
                .putExpressionName("#AttributeName$attributeName", attributeName)
                .putExpressionValue(
                    ":AttributeValue$attributeName",
                    table.tableSchema().attributeValue(oldEntry, attributeName)
                ).build()
        }.reduce(Expression::and)

        val request = PutItemEnhancedRequest.builder(clazz.java).item(newEntry)
            .conditionExpression(sameEntryExpression).build()
        try {
            table.putItem(request)
        } catch (e: ConditionalCheckFailedException) {
            return false
        }
        return true
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
        lateinit var newEntry: Lazy<EntryType>
        do {
            defaultEntry = default()
            entry = dynamoTable.getItem(defaultEntry)
            newEntry = lazy { update(entry!!) }
        } while (!if (entry == null) {
                putRequest(dynamoTable, defaultEntry, clazz)
            } else {
                updateRequest(dynamoTable, entry, newEntry.value, clazz)
            }
        )
        return if (entry == null) defaultEntry else newEntry.value
    }

    override fun <EntryType : Any> updateEntry(
        table: Table, keyValue: String, update: (EntryType) -> EntryType, clazz: KClass<EntryType>
    ): EntryType? {
        val dynamoTable = dbEnhancedClient.table(
            table.toString(), TableSchema.fromClass(clazz.java)
        )
        lateinit var entry: EntryType
        lateinit var newEntry: EntryType
        do {
            entry =
                dynamoTable.getItem(Key.builder().partitionValue(keyValue).build()) ?: return null
            newEntry = update(entry)
        } while (!updateRequest(dynamoTable, entry, newEntry, clazz)
        )
        return newEntry
    }

    override fun <EntryType : Any> putEntry(
        table: Table,
        entry: EntryType,
        clazz: KClass<EntryType>
    ): Boolean = putRequest(
        dbEnhancedClient.table(table.toString(), TableSchema.fromBean(clazz.java)),
        entry,
        clazz
    )
}

