package me.rivia.api.teams

interface TeamsClient {

    fun <T : Any> tokenOperation(tenantId: String, apiCall: (String) -> T?): T
    fun fetchAccessToken(
        tenantId: String,
        userRefreshToken: String? = null,
    ): Pair<String, String>?

    fun conditionalTokenOperation(
        tenantId: String, apiCall: (String) -> Boolean
    ) = tokenOperation(tenantId) {
        if (apiCall(it)) {
            true
        } else {
            null
        }
    }
}
