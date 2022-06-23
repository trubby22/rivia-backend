package me.rivia.api.graphhttp

import com.google.gson.Gson
import software.amazon.awssdk.http.HttpExecuteRequest
import software.amazon.awssdk.http.SdkHttpClient
import software.amazon.awssdk.http.SdkHttpMethod
import software.amazon.awssdk.http.SdkHttpRequest
import software.amazon.awssdk.http.apache.ApacheHttpClient
import java.net.URI
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
        if (!response.httpResponse().isSuccessful) {
            return null
        }
        return jsonParser.fromJson(
            String(response.responseBody().get().readAllBytes()), clazz.java
        )
    }
}
