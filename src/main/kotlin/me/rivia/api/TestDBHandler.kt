package me.rivia.api

import com.amazonaws.services.lambda.runtime.Context
import me.rivia.api.database.DynamoDatabase
import me.rivia.api.database.Table
import me.rivia.api.database.entry.Participant
import me.rivia.api.database.updateEntry

class TestDBHandler {
    val db = DynamoDatabase()
    fun test_db(input: Map<String, String>, context: Context): String {

//        test_put_user();


        test_update_user();
        return "executed"
    }

    // run when fakeUser does not exist in the database
    fun test_put_user() {
        val fakeUser: Participant = Participant("1", "Bob", "Uncle");
        val dummy : (Participant) -> Participant = {p : Participant -> p};
//        db.updateEntry(Table.PARTICIPANTS, fakeUser, (dummy));
    }

    // test once already exists in database
    fun test_update_user() {
        val fakeUser: Participant = Participant("1", "Bob", "Uncle");
        val fakeUser2: Participant = Participant("1", "Jane", "Aunt");
        val dummy : (Participant) -> Participant = { fakeUser2};
        db.updateEntry(Table.PARTICIPANTS, "1", (dummy));
    }

    fun test_get_user() {
        val fakeUser: Participant = Participant("1", "Bob", "Uncle");

    }


}
