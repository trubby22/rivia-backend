package main

import com.amazonaws.services.lambda.runtime.Context

class TestHandler2 {
    fun armInit(input: Map<String, String>, context: Context): String = "another duplicate lambda function"
}


