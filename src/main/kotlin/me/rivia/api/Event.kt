package me.rivia.api

class Event(var user: String?, var path: String?, var method: String?, var jsonData: Map<String, Any?>?) {
    constructor() : this(null, null, null, null)
}