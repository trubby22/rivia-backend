package me.rivia.api.database.impl

internal enum class Table(val tableName: String) {
    ORGANIZATIONS("Organizations"),
    MEETINGS("Meetings"),
    RESPONSEUSERS("ResponseUsers"),
    PARTICIPANTS("Participants"),
    RESPONSEPARTICIPANTS("ResponseParticipants"),
    PRESETQS("PresetQs"),
    RESPONSEPRESETQS("ResponsePresetQs");

    override fun toString(): String {
        return tableName
    }
}
