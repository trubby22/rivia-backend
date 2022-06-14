package me.rivia.api.database

import java.security.KeyStore.Entry
import kotlin.reflect.KClass

interface Database {
    fun <EntryType : Any> getEntry(
        table: Table,
        keyValue: String,
        clazz: KClass<EntryType>
    ): EntryType?

    fun <EntryType : Any> getEntries(
        table: Table,
        keys: Collection<String>,
        clazz: KClass<EntryType>
    ): List<EntryType>

    fun <EntryType : Any> updateEntry(
        table: Table,
        default: EntryType,
        update: (EntryType) -> EntryType,
        clazz: KClass<EntryType>
    ) : EntryType
}

inline fun <reified EntryType : Any> Database.getEntry(
    table: Table,
    keyValue: String
): EntryType? = this.getEntry(table, keyValue, EntryType::class)

inline fun <reified EntryType : Any> Database.getEntries(
    table: Table,
    keys: Collection<String>,
): List<EntryType> = this.getEntries(table, keys, EntryType::class)

inline fun <reified EntryType : Any> Database.updateEntry(
    table: Table,
    default: EntryType,
    noinline update: (EntryType) -> EntryType
): EntryType = this.updateEntry(table, default, update, EntryType::class)
