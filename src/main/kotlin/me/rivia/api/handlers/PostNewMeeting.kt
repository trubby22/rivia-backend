package me.rivia.api.handlers

import com.amazonaws.services.lambda.runtime.Context
import main.Meeting
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import java.util.function.Consumer


class PostNewMeeting {
    companion object {
        class ApiContext(cookie: Int?, val data: MeetingData?) {
            constructor() : this(null, null)
        }

        class MeetingData(val meeting: Meeting?, val participants: ArrayList<Uid>?) {
            constructor() : this(null, null)
        }
    }

    fun handle(input: ApiContext?, context: Context?) {
//        val ddb = DynamoDbClient.builder()
//            .region(Region.EU_WEST_2)
//            .build()
//
//        val enhancedClient = DynamoDbEnhancedClient.builder()
//            .dynamoDbClient(ddb)
//            .build()
//
//        // allows us to map the class with the fields stored in the table
//        val mappedTable: DynamoDbTable<MeetingDB> =
//            enhancedClient.table("Meeting", TableSchema.fromBean(MeetingDB::class.java))
//        val batchWriteItemEnhancedRequest: BatchWriteItemEnhancedRequest = BatchWriteItemEnhancedRequest.builder()
//            .writeBatches(
//                WriteBatch.builder(MeetingDB::class.java)
//                    .mappedTableResource(mappedTable)
//                    .addPutItem(Consumer<PutItemEnhancedRequest.Builder<T?>> { r: PutItemEnhancedRequest.Builder<T?> ->
//                        r.item(
//                            input
//                        )
//                    })
//                    .build()
//            )
//            .build()
    }
}
