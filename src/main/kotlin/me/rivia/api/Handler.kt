package me.rivia.api

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import me.rivia.api.database.Database
import me.rivia.api.database.DynamoDatabase
import me.rivia.api.handlers.*
import java.net.URL
import java.util.*

class Handler(val database: Database) : RequestHandler<Event, Response> {
    companion object {
        fun getPath(path: String): List<String> = path.split('/')

    }

    private val pathMappings =
        Trie<String, MutableMap<HttpMethod, Pair<Boolean, Lazy<SubHandler>>>>()

    init {
        registerSubHandler(listOf(), HttpMethod.GET, false, lazy { GetTenant() })
        registerSubHandler(listOf(), HttpMethod.POST, true, lazy { PostTenant() })
        registerSubHandler(listOf("meetings"), HttpMethod.GET, false, lazy { GetMeetings() })
        registerSubHandler(listOf("meetings"), HttpMethod.POST, false, lazy { PostMeeting() })
        registerSubHandler(listOf("meetings", null), HttpMethod.GET, false, lazy { GetMeeting() })
        registerSubHandler(
            listOf("meetings", null, "reviews"),
            HttpMethod.GET,
            false,
            lazy { GetReview() })
        registerSubHandler(
            listOf("meetings", null, "reviews"),
            HttpMethod.POST,
            false,
            lazy { PostReview() })
    }

    constructor() : this(DynamoDatabase())

    fun registerSubHandler(
        url: List<String?>,
        method: HttpMethod,
        withoutUser: Boolean,
        subHandler: Lazy<SubHandler>
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
                event.path ?: throw Error("Path field empty")
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
                return handler.value.handleRequest(path, tenant, null, jsonData, database)
            }
            if (event.user?.isEmpty() != false) {
                return Response(ResponseError.NOUSER)
            }
            return handler.value.handleRequest(
                path,
                tenant,
                event.user,
                jsonData,
                database
            )
        } catch (e: Error) {
            throw e
            return Response(ResponseError.EXCEPTION)
        }
    }
}
