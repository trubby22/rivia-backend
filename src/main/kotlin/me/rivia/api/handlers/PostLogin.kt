package me.rivia.api.handlers

import aws.smithy.kotlin.runtime.util.encodeToHex
import com.amazonaws.services.lambda.runtime.Context
import me.rivia.api.database.*
import me.rivia.api.database.Login as DbLogin
import me.rivia.api.database.Session as DbSession
import java.security.MessageDigest

// Session

class PostLogin : HandlerInit() {
    companion object {
        class LoginData(var email: String?, var password: String?) {
            constructor() : this(null, null)
        }

        class HttpResponse(val session: String?)
    }

    fun handle(input: LoginData?, context: Context?): HttpResponse? {
        val loginEntry = entryNullCheck<DbLogin>(getEntry<DbLogin>(Table.LOGIN, input?.email ?: return null) ?: return null, Table.LOGIN)
        val password = input.password ?: return null
        val hashedPassword = MessageDigest.getInstance("SHA-256").digest((password + loginEntry.salt!!).toByteArray()).encodeToHex()
        if (hashedPassword != loginEntry.password!!) {
            return null
        }
        lateinit var sessionEntry:DbSession
        do {
            sessionEntry = DbSession(
                cookie = generateId(),
                user = loginEntry.user!!
            )
        } while (!putEntry(Table.SESSION, sessionEntry))
        return HttpResponse(sessionEntry.cookie)
    }
}
