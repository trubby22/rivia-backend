package me.rivia.api.database

import software.amazon.awssdk.enhanced.dynamodb.*
import software.amazon.awssdk.enhanced.dynamodb.internal.EnhancedClientUtils.createKeyFromItem
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest
import software.amazon.awssdk.enhanced.dynamodb.model.ReadBatch
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties

class DynamoDatabase(region: Region = Region.EU_WEST_2) : Database {
    private companion object {
        const val SINGLE_BATCH_LIMIT = 100

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
        val dynamoTable: DynamoDbTable<EntryType> =
            dbEnhancedClient.table(table.toString(), TableSchema.fromClass(clazz.java))
        val entry =
            dynamoTable.getItem(Key.builder().partitionValue(keyValue).build()) ?: return null
        // Check for null entries
        return fieldNullCheck(entry, "entry from '$table' has a nulled component", clazz)
    }

    /**
     * Does a single batched get operation (up to SINGLE_BATCH_LIMIT entries) with the specified keys
     * from the DynamoDB
     */
    private fun <EntryType : Any> fetchSingleBatch(
        table: Table, keys: List<String>, clazz: KClass<EntryType>
    ): List<EntryType> {
        val dynamoTable: DynamoDbTable<EntryType> =
            dbEnhancedClient.table(table.toString(), TableSchema.fromClass(clazz.java))
        return dbEnhancedClient.batchGetItem { r ->
            r.addReadBatch(
                ReadBatch.builder(clazz.java).mappedTableResource(dynamoTable).apply {
                    repeat(keys.size) { index ->
                        this.addGetItem(
                            Key.builder().partitionValue(keys[index]).build()
                        )
                    }
                }.build()
            )
        }.resultsForTable(dynamoTable).toList()
    }

    /**
     * Gets the entries with the specified keys from the DynamoDB
     */
    override fun <EntryType : Any> getEntries(
        table: Table, keys: Collection<String>, clazz: KClass<EntryType>
    ): List<EntryType> {
        val currentBatchKeys: MutableList<String> = mutableListOf()
        val entries: MutableList<EntryType> = mutableListOf()
        for (key in keys) {
            currentBatchKeys.add(key)
            if (currentBatchKeys.size > SINGLE_BATCH_LIMIT) {
                entries.addAll(fetchSingleBatch(table, currentBatchKeys, clazz))
            }
        }
        entries.addAll(fetchSingleBatch(table, currentBatchKeys, clazz))
        // Check for null entries
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
        val sameEntryExpression = table.tableSchema().attributeNames().map {
            attributeName -> Expression.builder().expression("#keyName = :keyValue")
            .putExpressionName("#keyName", attributeName).putExpressionValue(
                ":keyValue",
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

    override fun <EntryType : Any> updateEntry(
        table: Table, default: EntryType, update: (EntryType) -> EntryType, clazz: KClass<EntryType>
    ): EntryType {
        val dynamoTable = dbEnhancedClient!!.table(
            table.toString(), TableSchema.fromClass(clazz.java)
        )
        var entry: EntryType?
        lateinit var newEntry: Lazy<EntryType>
        do {
            entry = dynamoTable.getItem(default)
            newEntry = lazy { update(entry!!) }
        } while (!if (entry == null) {
                putRequest(dynamoTable, default, clazz)
            } else {
                updateRequest(dynamoTable, entry, newEntry.value, clazz)
            }
        )
        return if (entry == null) default else newEntry.value
    }
}

