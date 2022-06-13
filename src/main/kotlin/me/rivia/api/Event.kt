package me.rivia.api

class Event(var cookie: String?, var url: String?, var method: String?, var jsonData: Map<String, Any?>?) {
    constructor() : this(null, null, null, null)
}
