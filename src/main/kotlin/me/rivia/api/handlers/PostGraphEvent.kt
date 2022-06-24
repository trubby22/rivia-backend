package me.rivia.api.handlers

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import me.rivia.api.Response
import me.rivia.api.database.Database
import me.rivia.api.database.Table
import me.rivia.api.database.entry.*
import me.rivia.api.database.getEntry
import me.rivia.api.database.putEntry
import me.rivia.api.encryption.CertificateStoreService
import me.rivia.api.graphhttp.MicrosoftGraphAccessClient
import me.rivia.api.graphhttp.MicrosoftGraphAccessClient.Companion.HttpMethod
import me.rivia.api.graphhttp.sendRequest
import me.rivia.api.teams.TeamsClient
import me.rivia.api.userstore.UserStore
import me.rivia.api.websocket.WebsocketClient
import java.time.Duration
import java.time.OffsetDateTime


class PostGraphEvent : SubHandler {
    companion object {
        data class DecryptedResourceData(
            @SerializedName("id") val id: String,
            @SerializedName("createdDateTime") val createdDateTime: String,
            @SerializedName("eventDetail") val eventDetail: EventDetail?,
            @SerializedName("subject") val subject: String?
        )

        data class EventDetail(
            @SerializedName("@odata.type") val odataType: String,
            @SerializedName("callDuration") val callDuration: String,
            @SerializedName("callParticipants") val callParticipants: List<Participant>,
            @SerializedName("initiator") val initiator: Participant
        )

        data class Participant(
            @SerializedName("user") val user: User
        )

        data class User(
            @SerializedName("id") val id: String
        )

        private data class PostMessageResponse(
            @SerializedName("id") val id: String
        )

        private data class Message(
            @SerializedName("body") val body: Body,
            @SerializedName("attachments") val attachments: List<Attachment>
        )

        private data class Body(
            @SerializedName("contentType") val contentType: String,
            @SerializedName("content") val content: String
        )

        private data class Attachment(
            @SerializedName("id") val id: String,
            @SerializedName("contentType") val contentType: String,
            @SerializedName("contentUrl") val contentUrl: String,
            @SerializedName("name") val name: String
        )

        private const val ATTACHMENT_ID = "153fa47d-18c9-4179-be08-9879815a9f90"
        private const val RIVIA_URL = "https://app.rivia.me"
        private const val RIVIA_NAME = "app.rivia.me"
    }

    private val jsonConverter = Gson()
    private val certificateStore = CertificateStoreService()

    override fun handleRequest(
        url: List<String>,
        _tenantId: String?,
        _userId: String?,
        validationToken: String,
        jsonData: Map<String, Any?>,
        database: Database,
        userStore: UserStore,
        userAccessToken: TeamsClient,
        applicationAccessToken: TeamsClient,
        graphAccessClient: MicrosoftGraphAccessClient,
        websocket: WebsocketClient
    ): Response {
        if (validationToken.isNotEmpty()) {
            return Response(validationToken)
        }

        val value = (jsonData["value"] as List<*>)[0] as Map<String, Any?>
        val resource = value["resource"] as String
        val tenantId = value["tenantId"] as String
        val encryptedContent = value["encryptedContent"] as Map<String, Any?>
        val encryptedData = encryptedContent["data"] as String
        val dataSignature = encryptedContent["dataSignature"] as String
        val dataKey = encryptedContent["dataKey"] as String
        val teamIdTemp: String =
            resource.split("teams")[1].split("/channels")[0]
        val teamId = teamIdTemp.substring(2, teamIdTemp.length - 2)
        val channelIdTemp: String =
            resource.split("/channels")[1].split("/messages")[0]
        val channelId = channelIdTemp.substring(2, channelIdTemp.length - 2)
        val messageIdTemp: String =
            resource.split("/messages")[1].split("/replies")[0]
        val messageId = messageIdTemp.substring(2, messageIdTemp.length - 2)

//        websocket.sendEvent(
//            { _, _ -> true }, encryptedData
//        )
//        websocket.sendEvent(
//            { _, _ -> true }, dataKey
//        )

        val decryptedResourceData = decryptData(
            dataKey, encryptedData, dataSignature
        )

//        val decryptedResourceData = try {
//            decryptData(dataKey, encryptedData)
//        } catch (e: Throwable) {
//            websocket.sendEvent(
//                { _, _ -> true },
//                "${e::class} ${e.message} ${e.stackTrace.asList()}"
//            )
//        }

//        websocket.sendEvent({ _, _ -> true }, decryptedResourceData.toString())

        if (decryptedResourceData?.eventDetail?.odataType?.contains("callEndedEventMessageDetail") == true) {

            val endTimeTemp = OffsetDateTime.parse(
                decryptedResourceData.createdDateTime
            )
            val duration = Duration.parse(
                decryptedResourceData.eventDetail.callDuration
            )
            val presetQs = database.getEntry<Tenant>(
                Table.TENANTS, tenantId
            )?.presetQIds ?: throw Error("Preset questions is null")
            val meetingId = decryptedResourceData.id
            val participants =
                decryptedResourceData.eventDetail.callParticipants.map { it.user.id }


            if (!database.putEntry(
                    Table.MEETINGS, Meeting(
                        meetingId,
                        tenantId,
                        decryptedResourceData.eventDetail.initiator.user.id,
                        participants,
                        decryptedResourceData.subject ?: "Teams meeting",
                        endTimeTemp.minus(duration).toEpochSecond().toInt(),
                        endTimeTemp.toEpochSecond().toInt(),
                        0,
                        presetQs,
                        listOf(),
                        listOf(),
                    )
                )
            ) {
                throw Error("Meeting already exists in the database")
            }
            for (presetQ in presetQs) {
                if (!database.putEntry(
                        Table.RESPONSEPRESETQS,
                        ResponsePresetQ(presetQ, meetingId, 0, 0)
                    )
                ) {
                    throw Error("ResponsePresetQ already in the database")
                }
            }
            for (participantId in participants) {
                if (!database.putEntry(
                        Table.RESPONSEDATAUSERS, ResponseDataUser(
                            tenantId,
                            participantId,
                            meetingId,
                            0,
                            0,
                            0,
                            0,
                        )
                    )
                ) {
                    throw Error("ResponseDataUser already in the database")
                }
            }

            sendChannelMessage(
                graphAccessClient,
                userAccessToken,
                tenantId,
                teamId,
                channelId,
                messageId
            )
        }

        return Response()
    }

    fun decryptData(
        dataKey: String,
        data: String,
        dataSignature: String,
    ): DecryptedResourceData? {
        val decryptedKey = certificateStore.getEncryptionKey(dataKey)
        return if (certificateStore.isDataSignatureValid(
                decryptedKey, data, dataSignature
            )
        ) {
            val decryptedData = certificateStore.getDecryptedData(
                decryptedKey, data
            )
            println(decryptedData)
            jsonConverter.fromJson(
                decryptedData, DecryptedResourceData::class.java
            )
        } else {
            null
        }
    }

    private fun sendChannelMessage(
        graphAccessClient: MicrosoftGraphAccessClient,
        userAccessToken: TeamsClient,
        tenantId: String,
        teamId: String,
        channelId: String,
        messageId: String,
    ) {
        val messageBody = Body(
            contentType = "html",
            content = "Please review the meeting. <attachment id=\"${ATTACHMENT_ID}\"></attachment>"
        )
        val attachment = Attachment(
            id = ATTACHMENT_ID,
            contentType = "reference",
            contentUrl = RIVIA_URL,
            name = RIVIA_NAME
        )
        val body = jsonConverter.toJson(
            Message(
                body = messageBody, attachments = listOf(attachment)
            )
        )

        userAccessToken.tokenOperation(tenantId) { token: String ->
            graphAccessClient.sendRequest<PostMessageResponse>(
                "https://graph.microsoft.com/v1.0/teams/${teamId}/channels/${channelId}/messages/${messageId}/replies",
                listOf(),
                HttpMethod.POST,
                listOf(
                    "Content-Type" to "application/json",
                    "Authorization" to "Bearer $token"
                ),
                body
            )
        }
    }
}

fun main() {
    val client = PostGraphEvent()

    println(
        client.decryptData(
            "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",
        )
    )

}
