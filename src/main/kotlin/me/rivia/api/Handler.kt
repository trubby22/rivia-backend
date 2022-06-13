package me.rivia.api

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import me.rivia.api.handlers.SubHandler
import java.net.URL
import java.util.*

class Handler : RequestHandler<Event, Any?> {
    companion object {
        fun getPath(url: String): List<String> = URL(url).path.split('/')

        fun getUser(cookie: String?): String? {
            if (cookie == null) {
                return null
            }
            TODO("Implement from a database")
        }
    }

    private val pathMappings = Trie<String, MutableMap<HttpMethod, Pair<Boolean, SubHandler>>>()

    init {

    }

    fun registerSubHandler(
        url: List<String?>,
        method: HttpMethod,
        withoutUser: Boolean,
        subHandler: SubHandler
    ) {
        var methodMappings = pathMappings[url]
        if (methodMappings == null) {
            methodMappings = EnumMap(HttpMethod::class.java)
            pathMappings[url] = methodMappings
        }
        methodMappings[method] = (withoutSession to subHandler)
    }

    override fun handleRequest(event: Event?, context: Context?): Response {
        if (event == null) {
            throw Error("Event not present")
        }
        val path = getPath(
            event.url ?: throw Error("Url field empty")
        )
        val (withoutSession, handler) = pathMappings[path]?.get(
            HttpMethod.valueOf(
                event.method ?: throw Error("Method field empty")
            )
        ) ?: return Response(ResponseError.NOHANDLER, null)
        val jsonData = event.jsonData ?: return Response(ResponseError.NODATA, null)

        if (withoutSession) {
            return handler.handleRequest(path, null, jsonData)
        }

        return handler.handleRequest(
            path,
            getUser(event.cookie) ?: return Response(ResponseError.NOSESSION, null),
            event.jsonData ?: return Response(ResponseError.NODATA, null)
        )
    }
}
