package me.rivia.api.handlers

import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.lambda.runtime.Context

class PostReview {
    companion object {
        class ApiContext(var meeting_id: Uid?, var cookie: Int?, var data: Review?) {
            constructor() : this(null, null, null)
        }

        class Review(
            val quality: Float?,
            val points: ArrayList<Uid>?,
            val not_needed: ArrayList<Uid>?,
            val not_prepared: ArrayList<Uid>?,
            val feedback: String?
        ) {
            constructor() : this( null, null, null, null, null)
        }
    }

    fun handle(input: ApiContext?, context: Context?) {
//        val rev : Review = Review(0.toFloat(),arrayListOf("1"), arrayListOf("1"), arrayListOf("1"), "0");
//        val input = ApiContext("0", 0, rev)
        val client: AmazonDynamoDB = AmazonDynamoDBClientBuilder
            .standard()
            .withRegion(Regions.EU_WEST_2)
            .build()
        val db = DynamoDB(client)
        val table = db.getTable("Review");
        val review : Review? = input!!.data;
        val item = Item()
            .withPrimaryKey("ReviewID", input.meeting_id)
            .withNumber("Cookie", input.cookie)
            .withString("UserID", "0")
            .withNumber("Quality", review?.quality)
            .withList("NotNeeded", review?.not_needed)
            .withList("NotPrepared", review?.not_prepared)
            .withList("PresetQ", review?.points)
            .withString("Feedback", review?.feedback)
        val outcome = table.putItem(item);
        if (outcome == null) {
            println("did not manage to create new item in database")
        }
    }
}
