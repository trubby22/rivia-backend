package me.rivia.api.database

import java.security.KeyStore.Entry
import kotlin.reflect.KClass

interface Database {
    fun <EntryType : Any> getEntry(
        table: Table,
        keyValue: String,
        clazz: KClass<EntryType>
    ): EntryType?

    fun <EntryType : Any> getAllEntries(
        table: Table,
        clazz: KClass<EntryType>
    ): List<EntryType>

    fun <EntryType : Any> updateEntryWithDefault(
        table: Table,
        default: () -> EntryType,
        update: (EntryType) -> EntryType,
        clazz: KClass<EntryType>
    ) : EntryType

    fun <EntryType : Any> updateEntry(
        table: Table,
        keyValue: String,
        update: (EntryType) -> EntryType,
        clazz: KClass<EntryType>
    ) : EntryType?

    fun <EntryType : Any> putEntry(
        table: Table,
        entry: EntryType,
        clazz: KClass<EntryType>
    ) : Boolean
}

inline fun <reified EntryType : Any> Database.getEntry(
    table: Table,
    keyValue: String
): EntryType? = this.getEntry(table, keyValue, EntryType::class)

inline fun <reified EntryType : Any> Database.getAllEntries(
    table: Table,
): List<EntryType> = this.getAllEntries(table, EntryType::class)

inline fun <reified EntryType : Any> Database.putEntry(
    table: Table,
    entry: EntryType
) : Boolean = this.putEntry(table, entry, EntryType::class)

inline fun <reified EntryType : Any> Database.updateEntryWithDefault(
    table: Table,
    noinline default: () -> EntryType,
    noinline update: (EntryType) -> EntryType
): EntryType = this.updateEntryWithDefault(table, default, update, EntryType::class)

inline fun <reified EntryType : Any> Database.updateEntry(
    table: Table,
    keyValue: String,
    noinline update: (EntryType) -> EntryType
): EntryType? = this.updateEntry(table, keyValue, update, EntryType::class)
