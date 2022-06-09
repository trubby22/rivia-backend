package me.rivia.api.handlers

import com.amazonaws.services.lambda.runtime.Context
import me.rivia.api.database.getEntry

class PostLogin {
    companion object {
        class LoginData(var email: String?, var password: String?) {
            constructor() : this(null, null)
        }

        class HttpResponse(val cookie: String, )
    }

    fun handle(input: LoginData?, context: Context?) :  {
        println(getEntry("Login","Email", input?.email ?: throw Error("No email")) )

    }
}
