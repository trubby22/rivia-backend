package me.rivia.api.handlers

import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.lambda.runtime.Context

class PostReview {
    companion object {
        class ApiContext(val meeting_id: Uid?, val cookie: Int?, val data: Review?) {
            constructor() : this(null, null, null)
        }

        class Review(
            val user_id: String?,
            val quality: Float?,
            val points: Array<Uid>?,
            val not_needed: Array<Uid>?,
            val not_prepared: Array<Uid>?,
            val feedback: String?
        ) {
            constructor() : this(null, null, null, null, null, null)
        }
    }

    fun handle(input: ApiContext?, context: Context?) {
        val client: AmazonDynamoDB = AmazonDynamoDBClientBuilder
            .standard()
            .withRegion(Regions.EU_WEST_2)
            .build()
        val db = DynamoDB(client)
        val table = db.getTable("Review");
        val review : Review? = input!!.data;
        val item = Item()
            .withPrimaryKey("ReviewID", input.meeting_id)
            .withString("UserID", review?.user_id)
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
