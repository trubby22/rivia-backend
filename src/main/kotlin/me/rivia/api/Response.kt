package me.rivia.api

enum class ResponseError(val id: Int) {
    NONE(0),
    NOHANDLER(1),
    NOSESSION(2),
    NODATA(3)
}

class Response(responseError: ResponseError, val jsonData: Any?) {
    val errorCode = responseError.id
}
