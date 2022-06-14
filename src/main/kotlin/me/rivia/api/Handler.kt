package me.rivia.api

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import me.rivia.api.database.Database
import me.rivia.api.database.DynamoDatabase
import me.rivia.api.handlers.SubHandler
import java.net.URL
import java.util.*

class Handler(val database: Database) : RequestHandler<Event, Response> {
    companion object {
        fun getPath(url: String): List<String> = URL(url).path.split('/').drop(1)

    }

    private val pathMappings = Trie<String, MutableMap<HttpMethod, Pair<Boolean, SubHandler>>>()

    init {

    }
    constructor() : this(DynamoDatabase())

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
        methodMappings[method] = (withoutUser to subHandler)
    }

    override fun handleRequest(event: Event?, context: Context?): Response {
        try {
            if (event == null) {
                throw Error("Event not present")
            }

            val pathRaw = getPath(
                event.url ?: throw Error("Url field empty")
            )

            if (pathRaw[0] != "tenants") {
                return Response(ResponseError.NOHANDLER)
            }

            val tenant = pathRaw[1]
            val path = pathRaw.drop(2)

            val (withoutUser, handler) = pathMappings[path]?.get(
                HttpMethod.valueOf(
                    event.method ?: throw Error("Method field empty")
                )
            ) ?: return Response(ResponseError.NOHANDLER)
            val jsonData = event.jsonData ?: mapOf()

            if (withoutUser) {
                return handler.handleRequest(path, tenant, null, jsonData, database)
            }
            return handler.handleRequest(
                path,
                tenant,
                event.user ?: return Response(ResponseError.NOUSER),
                jsonData,
                database
            )
        } catch (e: Error) {
            return Response(ResponseError.EXCEPTION)
        }
    }
}
