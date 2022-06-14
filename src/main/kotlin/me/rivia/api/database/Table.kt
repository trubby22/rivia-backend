package me.rivia.api.database

enum class Table(val tableName: String) {
    TENANTS("Tenants"),
    MEETINGS("Meetings"),
    PARTICIPANTS("Participants"),
    PRESETQS("PresetQs"),
    TENANTMEETINGS("TenantMeetings"), // For validating if a meeting is in an organization
    RESPONSETENANTUSERS("ResponseTenantUsers"),
    RESPONSEPARTICIPANTS("ResponseParticipants"),
    RESPONSEPRESETQS("ResponsePresetQs");

    override fun toString(): String {
        return tableName
    }
}
