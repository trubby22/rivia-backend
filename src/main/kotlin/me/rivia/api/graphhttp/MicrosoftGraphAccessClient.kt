package me.rivia.api.graphhttp

import kotlin.reflect.KClass

interface MicrosoftGraphAccessClient {
    companion object {
        enum class HttpMethod {
            GET,
            POST,
            PUT,
            DELETE,
            HEAD,
            PATCH,
            OPTIONS,
        }
    }

    fun <ResponseType : Any> sendRequest(
        url: String,
        queryArgs: List<Pair<String, List<String>>>,
        method: HttpMethod,
        headers: List<Pair<String, String>>,
        body: String,
        clazz: KClass<ResponseType>
    ): ResponseType?
}

inline fun <reified ResponseType : Any> MicrosoftGraphAccessClient.sendRequest(
    url: String,
    queryArgs: List<Pair<String, List<String>>>,
    method: MicrosoftGraphAccessClient.Companion.HttpMethod,
    headers: List<Pair<String, String>>,
    body: String
) = this.sendRequest(url, queryArgs, method, headers, body, ResponseType::class)
