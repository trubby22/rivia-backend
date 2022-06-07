package main

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
@DynamoDbBean
class Review (reviewID : String, var userID : String, var quality: Float,
                         var presetQ: List<Int>, var notNeeded: List<String>, var notPrepared : List<String>,
                         var feedback : String) {
    var reviewID = reviewID
        @DynamoDbPartitionKey get
        @DynamoDbPartitionKey set

}