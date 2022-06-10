package me.rivia.api.handlers

import aws.smithy.kotlin.runtime.util.encodeToHex
import com.amazonaws.services.lambda.runtime.Context
import me.rivia.api.database.*
import kotlin.random.Random
import me.rivia.api.database.User as DbUser
import me.rivia.api.database.Login as DbLogin

// User, Login, Session

class PostCreateAccount : HandlerInit() {
    companion object {
        class AccountData(
            var name: String?,
            var surname: String?,
            var email: String?,
            var password: String?
        ) {
            constructor() : this(null, null, null, null)
        }

        class HttpResponse(val session: String?)
    }

    fun handle(input: AccountData?, context: Context?) : HttpResponse? {
        if (input == null) {
            return null
        }
        lateinit var user: DbUser
        do {
            user = DbUser(
                userId = generateId(),
                email = input.email ?: return null,
                name = input.name ?: return null,
                surname = input.surname ?: return null,
            )
        } while (!putEntry(Table.USER, user))

        val salt = Random.nextBytes(16).encodeToHex()
        val loginEntry: DbLogin = DbLogin(
            email = input?.email ?: return null,
            password = hashPassword(input?.password ?: return null, salt),
            salt = salt,
            user = user.userId
        )
        if (!putEntry(Table.LOGIN, loginEntry)) {
            removeEntry<DbUser>(Table.USER, user.userId!!)
            return null
        }
        lateinit var sessionEntry: Session
        do {
            sessionEntry = Session(
                cookie = generateId(),
                user = loginEntry.user!!
            )
        } while (!putEntry(Table.SESSION, sessionEntry))
        return HttpResponse(sessionEntry.cookie)
    }
}
