package me.rivia.api.handlers

import com.amazonaws.regions.Regions
import com.amazonaws.services.lambda.runtime.Context

import com.amazonaws.AmazonServiceException
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome
import com.amazonaws.services.dynamodbv2.document.Table

class Playground {
    val client: AmazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
        .build()
    val dynamoDB: DynamoDB = DynamoDB(client)
    val table = dynamoDB.getTable("ProductCatalog")

    fun handler() {
        val item: Item = table.getItem("Id", 210)
    }

    fun handler2() {
        val relatedItems: MutableList<Number> = mutableListOf()
        relatedItems.addAll(listOf(341, 472, 649))

        val pictures: MutableMap<String, String> = mutableMapOf()
        pictures["FrontView"] = "xyz"
        pictures["RearView"]  = "xyz2"
        pictures["SideView"] = "xyz3"

        val reviews: MutableMap<String, List<String>> = mutableMapOf()

        val fiveStarReviews: MutableList<String> = mutableListOf()
        fiveStarReviews.addAll(listOf("Great", "Good", "Excellent"))

        reviews["FiveStar"] = fiveStarReviews

        val oneStarReviews: MutableList<String> = mutableListOf()
        oneStarReviews.add("Terrible")

        reviews["OneStar"] = oneStarReviews

        val item: Item = Item()
            .withPrimaryKey("Id", 123)
            .withString("Title", "Bicycle 123")
            .withString("Description", "123 description")
            .withNumber("Price", 500)
            .withStringSet("Color", "Red", "Black")
            .withStringSet("Mate", setOf("1", "2"))
            .withBoolean("InStock", true)
            .withNull("QuantityOnHand")
            .withList("Related")
            .withMap("Pictures", pictures)
            .withMap("Reviews", reviews)

        val outcome: PutItemOutcome = table.putItem(item)
    }

    fun handler3() {
        val item: Item = Item()
            .withPrimaryKey("Id", 104)
            .withString("Title", "Book 104 Title")
            .withString("ISBN", "4444")
            .withNumber("Price", 20)
            .withStringSet("Authos", setOf("Author1", "Author2"))

        val expressionAttributeValues: MutableMap<String, Any> = mutableMapOf()
        expressionAttributeValues[":val"] = "4444"

        val outcome: PutItemOutcome = table.putItem(
            item,
            "ISBN = :val",
            null,
            expressionAttributeValues
        )
    }

    fun handler4() {
        val vendorDocument: String = """
            {
            "V01": {
                "Name": "Acme Books",
                "Offices": ["Seattle"],
            },
            "V02": {
                "Name": "New Publishers, Inc.",
                "Offices": ["London", "New York"],
            },
            "V03": {
                "Name": "Better Buy Books",
                "Offices": ["Tokyo", "Los Angeles", "Sydney"],
            },
            }
        """.trimIndent()

        val item: Item = Item()
            .withPrimaryKey("Id", 210)
            .withJSON("VendorInfo", vendorDocument)

        val outcome: PutItemOutcome = table.putItem(item)
    }
}