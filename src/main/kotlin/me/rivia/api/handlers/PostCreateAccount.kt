package me.rivia.api.handlers

import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.lambda.runtime.Context
import me.rivia.api.database.putAccount


class PostCreateAccount {
    companion object {
        class AccountData(
            var name: String?,
            var surname: String?,
            var email: String?,
            var password: String?
        ) {
            constructor() : this(null, null, null, null)
        }
    }

    fun handle(input: AccountData?, context: Context?) {
        putAccount(input!!)
    }

//    {
//        "UserID": {
//        "S": "0"
//    },
//        "a": {
//        "S": "0"
//    },
//        "b": {
//        "S": "0"
//    },
//        "c": {
//        "S": "0"
//    },
//        "d": {
//        "S": ""
//    }
//    }
}
