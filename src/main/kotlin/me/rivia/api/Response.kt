package me.rivia.api

enum class ResponseError(val id: Int) {
    NONE(200),
    NOTENANT(401),
    NOUSER(402),
    NOHANDLER(400),
    EXCEPTION(500),
}

class Response private constructor(responseError: ResponseError, val jsonData: Any?) {
    val errorCode = responseError.id
    constructor(responseError: ResponseError) : this(responseError, null)
    constructor(jsonData: Any?) : this(ResponseError.NONE, jsonData)
}
