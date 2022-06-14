package me.rivia.api.handlers

import me.rivia.api.Response
import me.rivia.api.ResponseError
import me.rivia.api.database.*
import me.rivia.api.database.entry.PresetQ
import me.rivia.api.database.entry.Tenant

class PostTenant : SubHandler {
    override fun handleRequest(
        url: List<String>,
        tenant: String,
        user: String?,
        jsonData: Map<String, Any?>,
        database: Database
    ): Response {
        // Extracting json data
        val refreshToken = jsonData["refreshToken"]
        if (refreshToken !is String?) {
            return Response(ResponseError.WRONTENTRY)
        }
        val presetQTexts = jsonData["presetQs"]
        if (presetQTexts !is List<*>?) {
            return Response(ResponseError.WRONTENTRY)
        }
        if (presetQTexts != null) {
            for (text in presetQTexts) {
                if (text !is String) {
                    return Response(ResponseError.WRONTENTRY)
                }
            }
        }

        // Generating presetQIds
        val defaultPresetQs = lazy {
            database.getAllEntries<PresetQ>(Table.PRESETQS).filter {
                it.isDefault!!
            }
        }
        val defaultPresetQIds = lazy {
            defaultPresetQs.value.map { it.presetQId!! }
        }
        val presetQs = presetQTexts?.let {
            lazy {
                presetQTexts.map {
                    lateinit var presetQ: PresetQ
                    do {
                        presetQ = PresetQ(generateUid(), it as String, false)
                    } while (!database.putEntry(Table.PRESETQS, presetQ))
                    presetQ
                }
            }
        }
        val presetQIds = presetQs?.let {
            lazy {
                presetQs.value.map { it.presetQId!! }
            }
        }

        // Inserting the tenant entry
        val tenantEntry: Tenant = if (refreshToken == null) {
            database.updateEntry(Table.TENANTS, tenant) { tenantEntry: Tenant ->
                tenantEntry.presetQIds = presetQIds?.value ?: defaultPresetQIds.value
                tenantEntry
            } ?: return Response(ResponseError.NOTENANT)
        } else {
            database.updateEntryWithDefault(Table.TENANTS, {
                Tenant(
                    generateUid(), refreshToken, defaultPresetQIds.value, listOf()
                )
            }, { tenantEntry: Tenant ->
                tenantEntry.refreshToken = refreshToken
                if (presetQIds != null) {
                    tenantEntry.presetQIds = presetQIds.value
                }
                tenantEntry
            })
        }
        // tenantEntry.presetQIds will be either equal to presetQIds.value or defaultPresetQIds.value at this point
        assert(
            (defaultPresetQIds.isInitialized() && tenantEntry.presetQIds === defaultPresetQIds.value)
                    || (presetQIds != null && presetQIds.isInitialized() && tenantEntry.presetQIds === presetQIds.value)
        )
        val resultPresetQs =
            if (!defaultPresetQIds.isInitialized() || defaultPresetQIds.value !== tenantEntry.presetQIds) presetQs!!.value else defaultPresetQs.value
        return Response(resultPresetQs.map {
            me.rivia.api.handlers.responses.PresetQ(
                it.presetQId!!,
                it.text!!
            )
        })
    }
    }
}
