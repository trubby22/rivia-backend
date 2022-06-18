package me.rivia.api.handlers

import me.rivia.api.Response
import me.rivia.api.database.Database

fun interface SubHandler {
    fun handleRequest(url: List<String>, tenant: String, user: String?, jsonData: Map<String, Any?>, database: Database): Response
}
