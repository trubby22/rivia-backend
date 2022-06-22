package me.rivia.api.graphhttp

import com.google.gson.Gson
import me.rivia.api.teams.MicrosoftGraphClient
import software.amazon.awssdk.http.HttpExecuteRequest
import software.amazon.awssdk.http.SdkHttpClient
import software.amazon.awssdk.http.SdkHttpMethod
import software.amazon.awssdk.http.SdkHttpRequest
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import java.net.URI
import kotlin.reflect.KClass

class MicrosoftGraphHttpClient : MicrosoftGraphAccessClient {
    val httpClient: SdkHttpClient = UrlConnectionHttpClient.builder().build()
    val jsonParser = Gson()

    override fun <ResponseType : Any> sendRequest(
        url: String,
        queryArgs: List<Pair<String, List<String>>>,
        method: MicrosoftGraphAccessClient.Companion.HttpMethod,
        headers: List<Pair<String, String>>,
        body: String,
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

        val response = httpClient.prepareRequest(
            HttpExecuteRequest.builder().request(
                sdkHttpRequestBuilder.build()
            ).contentStreamProvider { body.byteInputStream() }.build()
        ).call()
        if (!response.httpResponse().isSuccessful) {
            throw Error(String(response.responseBody().get().readAllBytes()))
            return null
        }
        return jsonParser.fromJson(
            String(response.responseBody().get().readAllBytes()), clazz.java
        )
    }
}
