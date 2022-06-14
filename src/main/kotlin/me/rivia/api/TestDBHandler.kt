package me.rivia.api

import com.amazonaws.services.lambda.runtime.Context
import me.rivia.api.database.*
import me.rivia.api.database.entry.Participant
import me.rivia.api.database.entry.Tenant

class TestDBHandler {
    val db = DynamoDatabase()
    fun test_db(input: Map<String, String>, context: Context): String {

//        test_put_user();
//        test_update_user();
        test_put_tenant();
        return "executed"
    }

    // passes basic
    fun test_put_user() {
        val fakeUser: Participant = Participant("1", "Bob", "Uncle");
        val dummy : (Participant) -> Participant = {p : Participant -> p};
//        db.updateEntry(Table.PARTICIPANTS, fakeUser, (dummy));
    }

    // passes basic
    fun test_update_user() {
        val fakeUser: Participant = Participant("1", "Bob", "Uncle");
        val fakeUser2: Participant = Participant("1", "Jane", "Aunt");
        val dummy : (Participant) -> Participant = { fakeUser2};
        db.updateEntry(Table.PARTICIPANTS, "1", (dummy));
    }

    fun test_put_tenant() {
        val fakeTenant: Tenant = Tenant("1", "123", arrayListOf("2"), arrayListOf("3"));
        val dummy : (Tenant) -> Tenant = {t : Tenant -> t};
        db.putEntry(Table.TENANTS,fakeTenant);

    }


}