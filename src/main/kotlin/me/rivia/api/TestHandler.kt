package me.rivia.api

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import me.rivia.api.database.DynamoDatabase
import me.rivia.api.database.*

class TestHandler : RequestHandler<Map<String, Any>, String>{
    val db: Database = DynamoDatabase()
    override fun handleRequest(input: Map<String, Any>, context: Context): String {
        TODO("Empty")
    }
}
