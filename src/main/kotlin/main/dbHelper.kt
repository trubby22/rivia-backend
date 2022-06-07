package main

import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import me.rivia.api.Participant
import me.rivia.api.Response
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest


class dbHelper {

    // takes parameters chosen by user in front end and creates a new item in db using them
    // no need to use enhanced DynamoDB mapping here
    fun putReview(
        db: DynamoDB, reviewID: String, userID: String, quality: Float,
        presetQ: List<Int>, notNeeded: List<String>, notPrepared: List<String>,
        feedback: String
    ) {
        val table = db.getTable("Review");
        val item = Item()
            .withPrimaryKey("ReviewID", reviewID)
            .withString("UserID", userID)
            .withNumber("Quality", quality)
            .withList("NotNeeded", notNeeded)
            .withList("NotPrepared", notPrepared)
            .withList("PresetQ", presetQ)
            .withString("Feedback", feedback)
        val outcome = table.putItem(item);
    }

    // gets information from an item matching the reviewID
    fun getReview(enhancedClient: DynamoDbEnhancedClient, reviewID: String) {
        val mappedTable: DynamoDbTable<Review> =
            enhancedClient.table("Review", TableSchema.fromBean(Review::class.java))
        val key: Key = Key.builder()
            .partitionValue(reviewID)
            .build()

        val result: Review = mappedTable.getItem { r: GetItemEnhancedRequest.Builder -> r.key(key)
        }
    }

    // returns user information based of userID
//    fun getUser(db: DynamoDB, userID: String) {
//        val table = db.getTable("User");
//        val item = table.getItem("UserID", userID)
//    }
//
//    fun getMeeting(db: DynamoDB, meetingID: String) {
//        val table = db.getTable("Meeting");
//        val item = table.getItem("MetingID", meetingID)
//    }
//
//    fun getOrganisation(db: DynamoDB, organisationID: String) {
//        val table = db.getTable("Organisation");
//        val item = table.getItem("OrganisationID", organisationID)
//    }
}