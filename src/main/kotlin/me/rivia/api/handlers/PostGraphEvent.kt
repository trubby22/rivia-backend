package me.rivia.api.handlers

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import me.rivia.api.Response
import me.rivia.api.database.Database
import me.rivia.api.database.Table
import me.rivia.api.database.entry.*
import me.rivia.api.database.getEntry
import me.rivia.api.database.putEntry
import me.rivia.api.graphhttp.MicrosoftGraphAccessClient
import me.rivia.api.graphhttp.MicrosoftGraphAccessClient.Companion.HttpMethod
import me.rivia.api.graphhttp.sendRequest
import me.rivia.api.teams.TeamsClient
import me.rivia.api.userstore.UserStore
import me.rivia.api.websocket.WebsocketClient
import java.time.Duration
import java.time.OffsetDateTime
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import org.apache.commons.codec.binary.Base64 as ApacheBase64


class PostGraphEvent : SubHandler {
    companion object {
        private data class DecryptedResourceData(
            @SerializedName("id") val id: String,
            @SerializedName("createdDateTime") val createdDateTime: OffsetDateTime,
            @SerializedName("eventDetail") val eventDetail: EventDetail?,
            @SerializedName("subject") val subject: String?
        )

        private data class EventDetail(
            @SerializedName("@odata.type") val odataType: String,
            @SerializedName("callDuration") val callDuration: Duration,
            @SerializedName("callParticipants") val callParticipants: List<Participant>,
            @SerializedName("initiator") val initiator: Participant
        )

        private data class Participant(
            @SerializedName("user") val user: User
        )

        private data class User(
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
        private const val PRIVATE_KEY =
            "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"
    }

    private val jsonConverter = Gson()

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

        websocket.sendEvent(
            { _, _ -> true },
            "$value, $resource, $tenantId, $teamId, $channelId"
        )

        throw Error("Got here")

        val decryptedResourceData = decryptData(dataKey, encryptedData)

        if (decryptedResourceData.eventDetail?.odataType?.contains("callEndedEventMessageDetail") == true) {

            val endTimeTemp = decryptedResourceData.createdDateTime
            val duration = decryptedResourceData.eventDetail.callDuration
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

    private fun decryptData(
        dataKey: String, encryptedData: String
    ): DecryptedResourceData {
        val decodedKey: ByteArray = Base64.getDecoder().decode(PRIVATE_KEY)
        val asymmetricKey: SecretKey =
            SecretKeySpec(decodedKey, 0, decodedKey.size, "AES")
        val encryptedSymmetricKey: ByteArray =
            ApacheBase64.decodeBase64(dataKey)
        val cipher: Cipher =
            Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding")
        cipher.init(Cipher.DECRYPT_MODE, asymmetricKey)
        val decryptedSymmetricKey: ByteArray =
            cipher.doFinal(encryptedSymmetricKey)
        val skey: SecretKey = SecretKeySpec(decryptedSymmetricKey, "AES")
        val ivspec = IvParameterSpec(decryptedSymmetricKey.copyOf(16))
        val cipher2 = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        cipher2.init(Cipher.DECRYPT_MODE, skey, ivspec)
        val decryptedResourceString =
            String(cipher2.doFinal(ApacheBase64.decodeBase64(encryptedData)))
        return jsonConverter.fromJson(
            decryptedResourceString, DecryptedResourceData::class.java
        )
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
