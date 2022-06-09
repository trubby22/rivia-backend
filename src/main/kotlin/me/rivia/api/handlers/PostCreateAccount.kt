package me.rivia.api.handlers

import com.amazonaws.services.lambda.runtime.Context

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

    fun handle(input: AccountData?, context: Context?) : HttpResponse {
        TODO("Fill in")
    }
}
