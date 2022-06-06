package main

import com.amazonaws.services.lambda.runtime.Context

class TestHandler {
    fun handleRequest(input: Map<String, String>, context: Context): String = "abcdefgh"
}
