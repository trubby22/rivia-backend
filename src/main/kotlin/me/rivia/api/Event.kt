package me.rivia.api

class Event(var tenant: String?, var user: String?, var path: String?, var api: ApiEvent?, var jsonData: Map<String, Any?>?) {
    companion object {
        class ApiEvent(var type: String?, var method: String?) {
            constructor() : this(null, null)
        }
    }
    constructor() : this(null, null, null, null, null)
}
