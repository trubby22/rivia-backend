package main

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey

@DynamoDbBean
data class Meeting constructor(@get:DynamoDbPartitionKey var MeetingID: String? = "0",
                               val title: String? = "0", val startTime: Int? = 0,
                               val endTime: Int? = 0,
                               val participants : ArrayList<String>? = arrayListOf("1"),
                               val reviews : ArrayList<String>? = arrayListOf("1"),
                               val preset_qs : ArrayList<String>? = arrayListOf("1")) {

}