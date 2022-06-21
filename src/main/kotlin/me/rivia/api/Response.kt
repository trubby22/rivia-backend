package me.rivia.api

enum class ResponseError(val id: Int) {
    NONE(200),
    NOHANDLER(400),
    NOTENANT(401),
    NOUSER(402),
    WRONGUSER(403),
    NOMEETING(404),
    WRONGTENANTMEETING(405),
    WRONGUSERMEETING(406),
    WRONGENTRY(407),
    REVIEWSUBMITTED(408),
    EXCEPTION(500),
}

class Response private constructor(responseError: ResponseError, val jsonData: Any?) {
    val errorCode = responseError.id
    constructor(responseError: ResponseError) : this(responseError, null)
    constructor(jsonData: Any?) : this(ResponseError.NONE, jsonData)
    constructor() : this(mapOf<String, Any?>())
}
