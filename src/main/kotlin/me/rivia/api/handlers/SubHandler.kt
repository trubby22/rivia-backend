package me.rivia.api.handlers

import me.rivia.api.Response

interface SubHandler {
    fun handleRequest(url: List<String>, tenant: String, user: String, jsonData: Map<String, Any?>): Response
}
