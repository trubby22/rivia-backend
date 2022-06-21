package me.rivia.api.teams

import me.rivia.api.database.Database

enum class TokenType {
    APPLICATION,
    USER,
}

class MicrosoftGraphClient(private val database: Database, private val tokenType: TokenType) : TeamsClient {
    private fun getAccessToken(tenantId: String): String {
        TODO("Not yet implemented")
    }

    private fun refreshAccessToken(tenantId: String): String? {
        TODO("Not yet implemented")
    }

    override fun <T : Any> tokenOperation(tenantId: String, apiCall: (String) -> T?): T {
        var accessToken: String? = getAccessToken(tenantId)
        var result = apiCall(accessToken!!)
        while (result == null) {
            do {
                accessToken = refreshAccessToken(tenantId)
            } while (accessToken == null)
            result = apiCall(accessToken)
        }
        return result
    }

    override fun setRefreshToken(tenantId: String, refreshToken: String) {
        TODO("Not yet implemented")
    }
}
