package me.rivia.api.database

import javax.xml.crypto.dsig.keyinfo.KeyValue

interface Database {
    fun <EntryType> getEntry(
        table: Table,
        keyValue: String,
        clazz: Class<EntryType>
    ): EntryType?

    fun <EntryType> getEntries(
        table: Table,
        keys: Collection<String>,
        clazz: Class<EntryType>
    ): List<EntryType>
}

inline fun <reified EntryType> Database.getEntry(
    table: Table,
    keyValue: String
): EntryType? = this.getEntry(table, keyValue, EntryType::class.java)

inline fun <reified EntryType> Database.getEntries(
    table: Table,
    keys: Collection<String>,
): List<EntryType> = this.getEntries(table, keys, EntryType::class.java)

class DynamoDb {
    inline fun <EntryType> getEntry(
        table: Table,
        keyValue: String,
    ): EntryType? {
        return null
    }
}
