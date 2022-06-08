package me.rivia.api.handlers

import com.amazonaws.services.lambda.runtime.Context

class PostLogin {
    companion object {
        class LoginData(var email: String?, var password: String?) {
            constructor() : this(null, null)
        }
    }

    fun handle(input: LoginData?, context: Context?) {
        TODO("Fill in with database fetch")
    }
}
