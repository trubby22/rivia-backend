package me.rivia.api

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import me.rivia.api.database.Database
import me.rivia.api.database.DynamoDatabase
import me.rivia.api.handlers.*
import me.rivia.api.teams.MicrosoftGraphClient
import me.rivia.api.teams.TeamsClient
import me.rivia.api.teams.TokenType
import me.rivia.api.userstore.DatabaseUserStore
import me.rivia.api.userstore.UserStore
import me.rivia.api.websocket.ApiGatewayWebsocketClient
import me.rivia.api.websocket.WebsocketClient
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import java.util.*

class Handler(
    private val database: Database,
    private val userStore: UserStore,
    private val userTeamsClient: TeamsClient,
    private val applicationTeamsClient: TeamsClient,
    private val websocket: WebsocketClient
) : RequestHandler<Event, Response> {
    companion object {
        fun getPath(path: String): List<String> = path.split('/')

    }

    private val pathMappings =
        Trie<String, MutableMap<ApiMethod, Pair<Boolean, Lazy<SubHandler>>>>()

    init {
        registerSubHandler(listOf(), ApiMethod.HTTP_GET, false, lazy { GetTenant() })
        registerSubHandler(listOf(), ApiMethod.HTTP_POST, true, lazy { PostTenant() })
        registerSubHandler(listOf("meetings"), ApiMethod.HTTP_GET, false, lazy { GetMeetings() })
        registerSubHandler(listOf("meetings"), ApiMethod.HTTP_POST, false, lazy { PostMeeting() })
        registerSubHandler(listOf("meetings", null),
            ApiMethod.HTTP_GET,
            false,
            lazy { GetMeeting() })
        registerSubHandler(listOf("meetings", null, "reviews"),
            ApiMethod.HTTP_GET,
            false,
            lazy { GetReview() })
        registerSubHandler(listOf("meetings", null, "reviews"),
            ApiMethod.HTTP_POST,
            false,
            lazy { PostReview() })
        registerSubHandler(listOf("websockets", null),
            ApiMethod.WEBSOCKET_MESSAGE,
            false,
            lazy { WebsocketMessage() })
        registerSubHandler(listOf("websockets", null),
            ApiMethod.WEBSOCKET_DISCONNECT,
            true,
            lazy { WebsocketDisconnect() })
    }

    private constructor(
        database: Database, userTeamsClient: TeamsClient, applicationTeamsClient: TeamsClient
    ) : this(
        database,
        DatabaseUserStore(database, applicationTeamsClient),
        userTeamsClient,
        applicationTeamsClient,
        ApiGatewayWebsocketClient(database)
    )

    private constructor(database: Database) : this(
        database,
        MicrosoftGraphClient(database, TokenType.USER),
        MicrosoftGraphClient(database, TokenType.APPLICATION)
    )

    constructor() : this(DynamoDatabase())

    fun registerSubHandler(
        url: List<String?>, method: ApiMethod, withoutUser: Boolean, subHandler: Lazy<SubHandler>
    ) {
        var methodMappings = pathMappings[url]
        if (methodMappings == null) {
            methodMappings = EnumMap(ApiMethod::class.java)
            pathMappings[url] = methodMappings
        }
        methodMappings[method] = (withoutUser to subHandler)
    }

    override fun handleRequest(event: Event?, context: Context?): Response {
        try {
            if (event == null) {
                throw Error("Event not present")
            }
            if (event.api == null) {
                throw Error("Unknown api entrypoint")
            }
            if (event.api!!.type == null) {
                throw Error("Unknown api type")
            }
            if (event.api!!.method == null) {
                throw Error("Unknown api method")
            }

            val path = getPath(
                event.path ?: throw Error("Path field empty")
            )

            if (event.tenant?.isEmpty() != false) {
                return Response(ResponseError.NOTENANT)
            }

            val (withoutUser, handler) = pathMappings[path]?.get(
                ApiMethod.valueOf(
                    "${event.api!!.type!!}_${event.api!!.method!!}".uppercase()
                )
            ) ?: return Response(ResponseError.NOHANDLER)
            val jsonData = event.jsonData ?: mapOf()

            if (withoutUser) {
                return handler.value.handleRequest(
                    path,
                    event.tenant!!,
                    null,
                    jsonData,
                    database,
                    userStore,
                    userTeamsClient,
                    applicationTeamsClient,
                    websocket
                )
            }
            if (event.user?.isEmpty() != false) {
                return Response(ResponseError.NOUSER)
            }
            return handler.value.handleRequest(
                path,
                event.tenant!!,
                event.user!!,
                jsonData,
                database,
                userStore,
                userTeamsClient,
                applicationTeamsClient,
                websocket
            )
        } catch (e: Error) {
            throw e
            return Response(ResponseError.EXCEPTION)
        }
    }
}
