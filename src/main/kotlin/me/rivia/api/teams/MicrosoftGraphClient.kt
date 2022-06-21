package me.rivia.api.teams

import me.rivia.api.database.Database

enum class RefreshTokenType {
    APPLICATION,
    USER,
}

class MicrosoftGraphClient(private val database: Database, private val refreshTokenType: RefreshTokenType) : TeamsClient {
    private fun getAccessToken(tenantId: String): String {
        TODO("Not yet implemented")
    }

    private fun refreshAccessToken(tenantId: String): String {
        TODO("Not yet implemented")
    }

    override fun <T : Any> tokenOperation(tenantId: String, apiCall: (String) -> T?): T {
        var accessToken = getAccessToken(tenantId)
        var result = apiCall(accessToken)
        while (result == null) {
            accessToken = refreshAccessToken(tenantId)
        }
        return result
    }

    override fun setRefreshToken(tenantId: String, refreshToken: String) {
        TODO("Not yet implemented")
    }
}
