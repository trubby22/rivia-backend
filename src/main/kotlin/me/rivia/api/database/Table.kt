package me.rivia.api.database

enum class Table(val tableName: String) {
    // General Tables
    TENANTS("Tenants"),
    MEETINGS("Meetings"),
    PRESETQS("PresetQs"),
    USERS("Users"),
    // Response Tables
    RESPONSESUBMISSIONS("ResponseSubmissions"),
    RESPONSEDATAUSERS("ResponseDataUsers"),
    RESPONSEPRESETQS("ResponsePresetQs"),
    // Websocket Tables
    WEBSOCKETS("Websockets"),
    CONNECTIONS("Connections"),
    // Rating Tables
    OPINIONS("Opinions"),
    USAGES("Usages");

    override fun toString(): String {
        return tableName
    }
}
