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
import java.io.FileInputStream
import java.security.Key
import java.security.KeyStore
import java.time.Duration
import java.time.OffsetDateTime
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import org.apache.commons.codec.binary.Base64 as ApacheBase64


class PostGraphEvent : SubHandler {
    companion object {
        data class DecryptedResourceData(
            @SerializedName("id") val id: String,
            @SerializedName("createdDateTime") val createdDateTime: OffsetDateTime,
            @SerializedName("eventDetail") val eventDetail: EventDetail?,
            @SerializedName("subject") val subject: String?
        )

        data class EventDetail(
            @SerializedName("@odata.type") val odataType: String,
            @SerializedName("callDuration") val callDuration: Duration,
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
        private const val PRIVATE_KEY =
            "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCqSCgvPgE6pu+UQTXfJvokdczAeairD5skmw3E6hovalJbPHgxfsFhsTNXplWLHU4EQhQpskAlhiJFCb16Vmnml/vvDvEPc+6xY5kMTLyzeC+RjUbfT2o91gBRL0hquVgqN1zYOyHcTf7OBBhBY0N2klZSclxPGkuk5KaqX2e6lkpN9E/pUQ4E7Juec0m0+YsItZWrCjknu6tVy409yBfuygPcth3P6YJlkrj3EezSkj/vS4QGB0iWOnYUi0QRiFXV/VSOQanSmqsTQYdFjEgGYCDKLExRTuuiaJgGfIJBU/aZimAPbEfSFVxDd8RqBIbtXk4h3vDhWIXHvLuXniUxAgMBAAECggEAFNBgnxOubUep+b510grO4SWIC0/jotMzxMfyKStvW3Pc0LulunESvLj+/Fv/0VMZ7fHQyp738c5BQpyT7VACPlY+DITQSPIED2lL9CGuICbU867n5pRruVa+761+pdL1eKWPQvwJYccj2fVMrLNwM95j0EybxWyCqgtr5t/mCzuwGOTR1gehhAupxKqs3wUxI835kXB+XgYBxKo2jAiyFvnwxquNkIL9u/vU0OEZxhYdBEafGx2fjPYDmBvujBwMsQ5MN3SXf5rm4RZcg+MGlOtFs3SOEdXpxfbCcu3XhGhQySivGdaKcxp67vx7ERPalVoVig1tzRF7Iytf9v3f8QKBgQDUwCUO8EqkWE57ORuNtNiXtReRJ4+xh2Q9fxjh2pX3HseK12+lBwMj7cdUuiMzsnIHVFnBwpq1eA7N9pddTytfMXCBJQW7TTTk9nLZDhCqbggBN7USi943b5OMu+o9GDeRiX+18zefyNjMHdpDqorPj9z5E8xuALgJB0TJHMmGiQKBgQDM5eC+MXx6B9lv0pTsRfwdZ/x4hTv64wxDxQrQ9gnqDHKN1ku+xtIl9/rrCL3GqUwSJgJTCYjZ8q7Sm6OWnA87+GG9L1FsoBW63S5fZxbvlzbkY2y8JsxIByE/nW0928GXMjRoeDzL1t/e1ZLT28CDEaZXjR5dLycBBGzCB3h/aQKBgFwUkbWGgPfilI02qVtK05fNtFaJ5eJzdAo90TonjlK3yquoFNU7VLufNW/RZF0CteHAO1zmamlyfg3c1aR4GJCJ6GSs7m8jumoC1LBnKwVovCQyxTcyvEoIC7eUuYwZyCtAdLr+equ4lUS7AvXYgV3WqVBQpmFSBFU8BSR6RirpAoGAKZG5ttxzVI6jHnyqmgTY0V53ajwdnxjRNC6Vsy83c7RyyTPgyiOsHufgkC6F0fgm5sevUQluKkmCDlhlJ/khXhlWOvuOmWsg/XhCIDjZ/mG/UTaaqNF2QGax6CYYM6SR8grNdyRtsS023g6Wa0T6GZ1LEhgfQOiAEUTEi9jYKYECgYEAyljOfF4bC6TkSEgjEtcOxiFCHRi1584yoJaseQFh1xfW+ZnU/hvmn16/a98uyfMayJExFu+KDm1A4wW5pxode7rhk/GiWUgGaX5hNfocDMnOoT06LNmfiDUarD4oGT9dqgZXaBN82T3/WaU6MShg+DSsocLQHwHYOdN7zWn2Hz8="
        private const val BASE_64_JKSKEYSTORE =
            "MIIF0gIBAzCCBXwGCSqGSIb3DQEHAaCCBW0EggVpMIIFZTCCAkwGCSqGSIb3DQEHAaCCAj0EggI5MIICNTCCAjEGCyqGSIb3DQEMCgECoIIB0DCCAcwwZgYJKoZIhvcNAQUNMFkwOAYJKoZIhvcNAQUMMCsEFL2ESAvqL5oblb8ciHym9CwY5ALNAgInEAIBIDAMBggqhkiG9w0CCQUAMB0GCWCGSAFlAwQBKgQQTSK0kTGfFPyDZuIdfU6CGQSCAWC5iFsoOIZCRVe696seIIH2QAm3ex9EBOTLe053l2GEwHGbxROcZ32kuuo50bFzB4mw3xRzzIspwZ38RUN85Wj29TneAAaNNoGAMnebuZhgG4wNxSoYheivY4HiqtVfABeDw/XK9UKFUh7oj1n6gr6BwaAujoZTX44hrjP8xuq3OWrv2HSzSnky/GsQ1ank0zdb4XFa9XXeZsupr3vYWG7RflB+regNQozWwrHEHJSIuWbUQVydyM356gbvpVT0u0la37ZtUVZ+CwganlPd68d4dytChnAao0FjSXn++Ct0tGwFkrALKx58FLnFVKs9iERXafZRlJ85iF/Wh1zr7y5s3t8jVI2DIwOgm2sgFEnIRSbH4obDkPDl8NmK1X0Vcdy1RqqxBGoGJqbkWC/Ux75IngffS579VMHbgZP06TXwJ/9y0i+PF5JqZquYretipkC+ySmsDPfvJCVc2FhFn3SGMU4wKQYJKoZIhvcNAQkUMRweGgBzAGUAbABmAHMAaQBnAG4AZQBkAGoAawBzMCEGCSqGSIb3DQEJFTEUBBJUaW1lIDE2NTU5NjAzNTk4MjgwggMRBgkqhkiG9w0BBwagggMCMIIC/gIBADCCAvcGCSqGSIb3DQEHATBmBgkqhkiG9w0BBQ0wWTA4BgkqhkiG9w0BBQwwKwQU8tm41PX0UY+JiPARgKqo5w81s5wCAicQAgEgMAwGCCqGSIb3DQIJBQAwHQYJYIZIAWUDBAEqBBAeU3gZAdg6Srw2tCVHyCsvgIICgIqjgZ4j/+ql15cp+/TEspAa2gQMzBc+rVXABHGHE75Q+pDh2iJqBD9hmEHlpyC1VxJ2Zn9Vj7fEtR9PzRLGLWre0YRfBxEs8oosnnoGiHq88tRc/zVsA/1gNJ0IAG3XAcVpMJTefnPfuZz+rZoEIUE1bczNfhPj3xalliav0acsSkrJw8OftZIC5mU7d6fpKVDTNZVi6mB6t79+b8I0T7ksem+xecVG6PinLsMJMvRtSvYxxHVEdYYb5t02hGSBvKvUS1ANsy4d2MJbo4AV4IY8l99Su8WNkxYNqn58Pv+aVegC/UDBvYTlYEj1lv/N76WoAkg/vroUD65JOCOGgWMJMHcAflUp1wyruIhNy9i/QlVpAZTszuooC+WvjgeNAE1RijvuIdpQ1cKrqzbU7cuTz7OyovF/W4RBjZbZM96HhaHhv7leo4gDXYDiZ4fIAS5sVJyLT+cm2YmMpXv0kAMEu9aZZ7q0LRlPGDPbzKxXtEss9haiPEPwoT+8AcE91ct2R+OEqydrlIgpuW1Es6Lj2fTL3jqVITrA42r5AEIPLHRQgTO0tk0c5HNnLZ0m7w45LU+et7ksPwI44NU5MwG6D3LCjXUSdNoe70ecWETYCOSDXBts/byjCqQdgeBpzlAMaCoAJ9A9xRHlaier6CyY0xYGI2iXjogV8LP5c9qBHugtqkhR3+q3RiTDowY5EyWVcQq9eKJQBDSgMCq7wi1jqDA0LjDGIK4QBGYkUp/y4PmQDhAzrHTQctfYGsNNfev0s8mrGv6AxBtKYsHcYb3myt9BqQVSGi5gm3l3+1GJDgbQHMIYPdnIAEtzTJr4xLFzcbZvf38qbufJ3OBBRIcwTTAxMA0GCWCGSAFlAwQCAQUABCAoSB5WeM6jUKPHmXFrKJViRHNSF/qDrYb0N3imEB9VbQQUXb3CBMZJBpG5mVQ7UOJYB6FDwkgCAicQ"
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
            { _, _ -> true }, encryptedData
        )
        websocket.sendEvent(
            { _, _ -> true }, dataKey
        )


        val decryptedResourceData = try {
            decryptData(dataKey, encryptedData)
        } catch (e: Throwable) {
            websocket.sendEvent(
                { _, _ -> true },
                "${e::class} ${e.message} ${e.stackTrace.asList()}"
            )
            throw Error("There")
        }

        websocket.sendEvent({ _, _ -> true }, decryptedResourceData.toString())
        throw Error("Here")

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

    fun decryptData(
        dataKey: String, encryptedData: String
    ): DecryptedResourceData {
//        val decodedKey: ByteArray = Base64.getDecoder().decode(PRIVATE_KEY)
//        val asymmetricKey: SecretKey =
//            SecretKeySpec(decodedKey, 0, decodedKey.size, "RSA")
        val storepass = "123456"
        val alias =
            "selfsignedjks" //alias of the certificate when store in the jks store, should be passed as encryptionCertificateId when subscribing and retrieved from the notification
        val ks = KeyStore.getInstance("JKS")
//        ks.load(
//            Base64.getDecoder().decode(BASE_64_JKSKEYSTORE).inputStream(),
//            storepass.toCharArray()
//        )
        ks.load(FileInputStream("JKSkeystore.jks"), storepass.toCharArray())
        val asymmetricKey: Key = ks.getKey(alias, storepass.toCharArray())
        val encryptedSymmetricKey: ByteArray =
            ApacheBase64.decodeBase64(dataKey)
        println(encryptedSymmetricKey.size)
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

fun main() {
    val client = PostGraphEvent()
    client.decryptData(
        dataKey = "JMqzOVSXLQ2yAORXipEUC+CWCaULZLMnoRuRDaKc6xB3PQGLJXBSzAwjEyVcjdO42tgBrbkI39L9/bD1puSqWw6m21XaJ93QWdAGT8pMTLWeYCxoYHmps/kkXJjECAuRdD7QdikII8v97zHAL4ywbWx+M2uqtWn2NtN5LmwZNBG1Yl7nBwPFfByPjiW2ZPAbvWcwfaCcdW+XFWY7aqVFysGpNo1iauIR+2LenNX8k3YUgxQMaJ8UerLPIZ4B80qKg0/t4QffkfOaWPLmbz2EI98pV4qC+u/yvXstUCYpbvXD+4N2qp9ACJwl7m62qGD3Epi4u4v991Qty0Td2veYwg==",
        encryptedData = "JBgGlTEu0K7ihJ8A1GWDmmxDs6UR4Dh2GAu2WiTeIk5XiQnYto7rTOkHr0q9cA5918oZPGp4cdIj8RjKK0v0jowOJauCU/3Pp0zAv3bdGBLGPaX1695ck2aPq3o72nhjDZrcawgLx4Vc34irstA4+i86RaxP8Lb2PEOXIZnIPc4ne/XYADuYm1isRKJNSqAPc5MC0YzGyOvK8Vitw1JDJeD09ssvW6PFgSvd/Hs3IsRNZuiAk5eAm0tClriZF9CNwMKLLdO6ij/ZjCM0lK7+KZ0kKunbTzp/n0avVcFiIZ0LBi0xR1WjV97dWEgx00Tp2ept0Ep+/CpByGndcYx2MQBsxJYNQVNPnFEKqij1UGAbeS1eVg6Mlf2g8E6jBBpNYVN5dA+8a3748lPCS0ExcAhci9eFg28yFy/4qGa7kfl5ke1sgr54K0SGg6xwsX+OBe0v7gVPiqaFyttDJ5EgTH/FJ81HiNScjieLDjJK68fKcwVVTZ+hVoXkS1Mpx3vzSi56QQ9BE+5HEojHTsAja3iOjoe6hgkFEkL7I8g3bo0hpQrmLPEgil5VZzyRyE6IWoozlGcIh4CoGVL4W92SrefrUC2Cu5dB967/fXp3tKQpmKuLYdR6EnnWHpD6au1nmrXXE20eFnO5H8sC6a4m/9nTAcijwyRcQZ90ZslGw18OZi87C+TEaoryQ80PDLLXoRhRrvMhrZ1N/cbPv3ui6PvXqj3NFcVwN8hTvaQts9QFN03bOj1QH+7bdpM0v4YYbtDFSupoexFiLMsXT2OSbvA7EJ2wBvswnm8k8gPhfJxwBAH4dIuYP4niWKuOkIITvbA0CDZr31Lm0etJfs8lZf5M4bYxJdha/l8wkqsZaQSk5lleh3hepQvG1bqcN86EPvbRybeqH8L9aRbpzfZm7ShlKdIG0B6bxO4SVdnJSWnHtJcPMNTOREd2+eLR9V3mvtskY4kDI+yIYJDxyXwmlU4QMDd+4vy1aE/hX0GgD+68LtxW4qcU4LAv8olO+DPfR80oMja9z5O1l+01HfGyyFAVWtlyy+iXhnhDhSTFLpuYqnzZRzSRBGKLEcLlB/RUlq1XMWkynhCd80cyyaqDZvwDC3+6vQWB9dhICOnxhD1Z6Qpf+iTm1gtPuuTCORNUM/Lz5zUrCDqiULJUwmfC5gwvWjUpcddab28fKvI1FYfpewoJNKqySWsECsISb8MjuIkkWY3nneq/Jhkh82bTLunMFosEkYHUk3G/gVy3M/QTIupgg3dKNwtG6TdXWJfKcPxie0ooEAs8RYzg7/hxUx6sqJj45XrMt2V5qVXja42Y9uqxtsa2BeBu8fqDgrBmSVD1kSAFNRVRBqAEr3/r4d1nJXg41bcOXi3dx8kehOymAXOKl2Xg6jeT76I9N+FUfwYOdxbUekKb3XqYbq/uMQP4sm8BKUhQM7IVDYvuGdhladMWdH/UVtfcjiBhsJoGx02jzkE8tpTFXQNU8Qw+hReplA6YVj0jYJUH72sgvbyUyqbc2qNmtIPMNjyJiR2ot6ObjuJDFDhJjStflNfgbu9tOnyYlnbNoxB+kR5Qd+f88uNPSgBSjLkon0tPGeyBAr6coJerGPHO6uuPHPoLTEzSvV38JXj5U6UDyxl5PhqCo34UQ28GVP5K9vik+he2AWo80Ui/yMuiP/dkdnqHE/FOen7U4S76yQ59M0QCvQfHyoMj/ZIShoTd4hTlLiK41NG8zV5agsnOFhx/OpzujUAveFrrkyqqqEHxhed98AFM8XdhZFmFajsDGpIrx8WcsCmbkZE/QGD0NfBM5OOcRA3APzgYUy2bbeNj9FWsEEk=",
    )
}
