package me.rivia.api.graphhttp

import com.google.gson.Gson
import software.amazon.awssdk.http.HttpExecuteRequest
import software.amazon.awssdk.http.SdkHttpClient
import software.amazon.awssdk.http.SdkHttpMethod
import software.amazon.awssdk.http.SdkHttpRequest
import software.amazon.awssdk.http.apache.ApacheHttpClient
import java.io.IOException
import java.net.URI
import java.nio.charset.StandardCharsets
import kotlin.reflect.KClass

class MicrosoftGraphHttpClient : MicrosoftGraphAccessClient {
    val httpClient: SdkHttpClient = ApacheHttpClient.builder().build()
    val jsonParser = Gson()

    override fun <ResponseType : Any> sendRequest(
        url: String,
        queryArgs: List<Pair<String, List<String>>>,
        method: MicrosoftGraphAccessClient.Companion.HttpMethod,
        headers: List<Pair<String, String>>,
        body: String?,
        clazz: KClass<ResponseType>
    ): ResponseType? {
        var sdkHttpRequestBuilder = SdkHttpRequest.builder().uri(
            URI.create(
                url
            )
        ).method(SdkHttpMethod.valueOf(method.toString()))
        for ((paramName, paramValues) in queryArgs) {
            sdkHttpRequestBuilder = sdkHttpRequestBuilder.appendRawQueryParameter(paramName, paramValues.joinToString(","))
        }
        for ((headerName, headerValue) in headers) {
            sdkHttpRequestBuilder = sdkHttpRequestBuilder.appendHeader(headerName, headerValue)
        }

        var httpExecuteRequestBuilder = HttpExecuteRequest.builder().request(
            sdkHttpRequestBuilder.build()
        )
        if (body != null) {
            httpExecuteRequestBuilder = httpExecuteRequestBuilder.contentStreamProvider { body.byteInputStream() }
        }

        val response = httpClient.prepareRequest(
            httpExecuteRequestBuilder.build()
        ).call()
//        BEGINNNING OF NEW STUFF
        val statusCode = response.httpResponse().statusCode()
        var bytes: ByteArray? = null
        try {
            bytes =
                response.responseBody().orElseThrow().delegate().readAllBytes()
        } catch (e: NoSuchElementException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        var responseBody: String? = null
        if (bytes != null) {
            responseBody = String(
                bytes,
                StandardCharsets.UTF_8
            )
        }
        if (!response.httpResponse().isSuccessful) {
            throw Error(statusCode.toString() + responseBody)
        }
        println(statusCode)
        println(responseBody)
//        END OF NEW STUFF
        if (!response.httpResponse().isSuccessful) {
            return null
        }
        return jsonParser.fromJson(
            String(response.responseBody().get().readAllBytes()), clazz.java
        )
    }
}
