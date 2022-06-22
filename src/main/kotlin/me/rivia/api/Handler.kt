package me.rivia.api

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import me.rivia.api.database.Database
import me.rivia.api.database.DynamoDatabase
import me.rivia.api.graphhttp.MicrosoftGraphAccessClient
import me.rivia.api.graphhttp.MicrosoftGraphHttpClient
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
    private val graphAccessClient: MicrosoftGraphAccessClient,
    private val userStore: UserStore,
    private val userTeamsClient: TeamsClient,
    private val applicationTeamsClient: TeamsClient,
    private val websocket: WebsocketClient
) : RequestHandler<Event, Response> {
    companion object {
        fun getPath(path: String): List<String> = path.split('/')

    }

    private val pathMappings =
        Trie<String, MutableMap<ApiMethod, Triple<Boolean, Boolean, Lazy<SubHandler>>>>()

    init {
        // Frontend handlers
        registerSubHandler(listOf(), ApiMethod.HTTP_GET, false, false, lazy { GetTenant() })
        registerSubHandler(listOf(), ApiMethod.HTTP_POST, false, true, lazy { PostTenant() })
        registerSubHandler(
            listOf("meetings"),
            ApiMethod.HTTP_GET,
            false,
            false,
            lazy { GetMeetings() })
        registerSubHandler(listOf("meetings", null),
            ApiMethod.HTTP_GET,
            false, false,
            lazy { GetMeeting() })
        registerSubHandler(listOf("meetings", null, "reviews"),
            ApiMethod.HTTP_GET,
            false, false,
            lazy { GetReview() })
        registerSubHandler(listOf("meetings", null, "reviews"),
            ApiMethod.HTTP_POST,
            false, false,
            lazy { PostReview() })
        registerSubHandler(
            listOf("rating"),
            ApiMethod.HTTP_POST,
            false,
            false,
            lazy { PostRating() })
        registerSubHandler(
            listOf("timing"),
            ApiMethod.HTTP_POST,
            false,
            false,
            lazy { PostTiming() })

        // Teams integration handlers
        registerSubHandler(
            listOf("subscription"),
            ApiMethod.HTTP_POST,
            true,
            true,
            lazy { PostSubscription() })
        registerSubHandler(
            listOf("graphEvent"),
            ApiMethod.HTTP_POST,
            true,
            true,
            lazy { PostGraphEvent() })

        // Websocket handlers
        registerSubHandler(listOf("websockets", null),
            ApiMethod.WEBSOCKET_MESSAGE,
            false, false,
            lazy { WebsocketMessage() })
        registerSubHandler(listOf("websockets", null),
            ApiMethod.WEBSOCKET_DISCONNECT,
            true, true,
            lazy { WebsocketDisconnect() })
    }

    private constructor(
        database: Database, graphAccessClient: MicrosoftGraphAccessClient, userTeamsClient: TeamsClient, applicationTeamsClient: TeamsClient
    ) : this(
        database,
        graphAccessClient,
        DatabaseUserStore(database, graphAccessClient, applicationTeamsClient),
        userTeamsClient,
        applicationTeamsClient,
        ApiGatewayWebsocketClient(database)
    )

    private constructor(database: Database, graphAccessClient: MicrosoftGraphAccessClient) : this(
        database,
        graphAccessClient,
        MicrosoftGraphClient(database, graphAccessClient, TokenType.USER),
        MicrosoftGraphClient(database, graphAccessClient, TokenType.APPLICATION)
    )

    constructor() : this(DynamoDatabase(), MicrosoftGraphHttpClient())

    fun registerSubHandler(
        url: List<String?>,
        method: ApiMethod,
        withoutTenant: Boolean,
        withoutUser: Boolean,
        subHandler: Lazy<SubHandler>
    ) {
        var methodMappings = pathMappings[url]
        if (methodMappings == null) {
            methodMappings = EnumMap(ApiMethod::class.java)
            pathMappings[url] = methodMappings
        }
        methodMappings[method] = Triple(withoutTenant, withoutUser, subHandler)
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

            val (withoutTenant, withoutUser, handler) = pathMappings[path]?.get(
                ApiMethod.valueOf(
                    "${event.api!!.type!!}_${event.api!!.method!!}".uppercase()
                )
            ) ?: return Response(ResponseError.NOHANDLER)
            val jsonData = event.jsonData ?: mapOf()

            if (withoutTenant) {
                return handler.value.handleRequest(
                    path,
                    null,
                    null,
                    jsonData,
                    database,
                    userStore,
                    userTeamsClient,
                    applicationTeamsClient,
                    graphAccessClient,
                    websocket
                )
            }
            if (event.tenant?.isEmpty() != false) {
                return Response(ResponseError.NOTENANT)
            }
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
                    graphAccessClient,
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
                graphAccessClient,
                websocket
            )
        } catch (e: Error) {
            throw e
            return Response(ResponseError.EXCEPTION)
        }
    }
}
