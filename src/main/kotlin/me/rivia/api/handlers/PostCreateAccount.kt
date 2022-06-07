package me.rivia.api.handlers

import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.lambda.runtime.Context



class PostCreateAccount {
    companion object {
        class AccountData(
            val name: String?,
            val surname: String?,
            val email: String?,
            val password: String?
        ) {
            constructor() : this(null, null, null, null)
        }
    }

    val dummyUserId: String = "0";

    fun handle(input: AccountData?, context: Context?) {
        val client: AmazonDynamoDB = AmazonDynamoDBClientBuilder
            .standard()
            .withRegion(Regions.EU_WEST_2)
            .build()
        val db = DynamoDB(client)
        val table = db.getTable("User");
        if (input == null) {
            println("input is null");
        }
        val item = Item()
            .withString("UserID", genUserId())
//            .withString("name", input!!.name)
//            .withString("surname", input!!.surname)
//            .withString("email", input!!.email)
//            .withString("password", input!!.password)
            .withString("name", "0")
            .withString("surname", "0")
            .withString("email", "0")
            .withString("password", "0")
        val outcome = table.putItem(item);
        if (outcome == null) {
            println("did not manage to create new item in database")
        }
    }

    fun genUserId() : String {
        return (dummyUserId.toInt() + 1).toString();
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
