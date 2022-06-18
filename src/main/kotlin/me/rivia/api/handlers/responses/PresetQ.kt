package me.rivia.api.handlers.responses

import me.rivia.api.database.entry.PresetQ as DatabasePresetQ
class PresetQ(val id: String, val text: String) {
    internal constructor(presetQ: DatabasePresetQ) : this(presetQ.presetQId!!, presetQ.text!!)
}
