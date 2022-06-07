package main

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey

@DynamoDbBean
class MeetingDB constructor(meetingID: String, val title: String, val startTime: Int, val endTime: Int,
                            val participants : Array<Int>, val reviews : Array<Int>,
                            val preset_qs : Array<String>) {
    var meetingID = meetingID
        @DynamoDbPartitionKey get
        @DynamoDbPartitionKey set

}