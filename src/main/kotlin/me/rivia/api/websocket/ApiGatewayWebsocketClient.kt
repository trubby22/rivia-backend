package me.rivia.api.websocket

import com.amazonaws.lambda.thirdparty.com.google.gson.Gson
import me.rivia.api.database.*
import me.rivia.api.database.entry.Connection
import me.rivia.api.database.entry.Websocket
import org.jetbrains.annotations.TestOnly
import software.amazon.awssdk.awscore.AwsRequestOverrideConfiguration
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.protocols.json.internal.marshall.JsonMarshaller
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiClient
import software.amazon.awssdk.services.apigatewaymanagementapi.model.PostToConnectionRequest
import java.net.URI

class ApiGatewayWebsocketClient(private val database: Database, region: Region = Region.EU_WEST_2) :
    WebsocketClient {
    companion object {
        const val URL = "https://websocket.api.rivia.me"
    }

    private val httpClient = UrlConnectionHttpClient.builder().build()

    private val wsClient =
        ApiGatewayManagementApiClient.builder().httpClient(httpClient).region(region)
            .endpointOverride(URI(URL)).build()

    private val jsonConverter = Gson()

    override fun registerWebsocket(connectionId: String, tenantId: String, userId: String) {
        database.updateEntryWithDefault(
            Table.WEBSOCKETS,
            { Websocket(connectionId, tenantId, userId) },
            { Websocket(connectionId, tenantId, userId) })
        database.updateEntryWithDefault(
            Table.WEBSOCKETS,
            { Connection(tenantId, userId, listOf(connectionId)) },
            { connectionEntry ->
                connectionEntry.connectionIds =
                    connectionEntry.connectionIds!! + listOf(connectionId); connectionEntry
            })
    }

    override fun unregisterWebsocket(connectionId: String) {
        val websocketEntry = database.deleteEntry<Websocket>(Table.WEBSOCKETS, connectionId)
        if (websocketEntry != null) {
            database.updateEntry<Connection>(Table.WEBSOCKETS, Connection(websocketEntry.tenantId!!, websocketEntry.userId!!, null).tenantIdUserId!!) {
                it.connectionIds =
                    it.connectionIds!!.filter { otherConnectionId: String ->
                        !(connectionId::equals)(
                            otherConnectionId
                        )
                    }; it
            }
        }
    }

    override fun sendEvent(tenantId: String, userId: String, event: Any) {
        val jsonString = jsonConverter.toJson(event)
        val jsonBytes = jsonString.toByteArray()
        val jsonData = SdkBytes.fromByteArray(jsonBytes)
        val connectionIds = database.getEntry<Connection>(Table.CONNECTIONS, Connection(tenantId, userId, null).tenantIdUserId!!)?.connectionIds ?: listOf()
        for (connectionId in connectionIds) {
            val request = PostToConnectionRequest.builder().connectionId(connectionId).data(jsonData).build()
            wsClient.postToConnection(request)
        }
    }
}
