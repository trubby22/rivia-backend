package me.rivia.api.handlers

import me.rivia.api.Response
import me.rivia.api.ResponseError
import me.rivia.api.database.*
import me.rivia.api.database.entry.PresetQ
import me.rivia.api.database.entry.Tenant
import me.rivia.api.handlers.responses.PresetQ as ResponsePresetQ

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
            return Response(ResponseError.WRONGENTRY)
        }
        val presetQTexts = jsonData["presetQs"]
        if (presetQTexts !is List<*>?) {
            return Response(ResponseError.WRONGENTRY)
        }
        if (presetQTexts != null) {
            for (text in presetQTexts) {
                if (text !is String) {
                    return Response(ResponseError.WRONGENTRY)
                }
            }
        }

        // Generating presetQIds
        val defaultPresetQs = lazy {
            database.getAllEntries<PresetQ>(Table.PRESETQS).filter {
                it.isdefault!!
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
        val tenantEntry: Tenant =
            if (refreshToken == null) {
                database.updateEntry(Table.TENANTS, tenant) { tenantEntry: Tenant ->
                    tenantEntry.presetQIds = presetQIds?.value ?: defaultPresetQIds.value
                    tenantEntry
                } ?: return Response(ResponseError.NOTENANT)
            } else {
                database.updateEntryWithDefault(Table.TENANTS, {
                    Tenant(
                        tenant, refreshToken, defaultPresetQIds.value, listOf()
                    )
                }, { tenantEntry: Tenant ->
                    tenantEntry.refreshToken = refreshToken
                    if (presetQIds != null) {
                        tenantEntry.presetQIds = presetQIds.value
                    }
                    tenantEntry
                })
            }

        return Response(tenantEntry.presetQIds!!.map {
            ResponsePresetQ(database.getEntry(Table.PRESETQS, it) ?: throw Error("presetQ not present"))
        })
    }
}
