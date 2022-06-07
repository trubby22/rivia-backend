package me.rivia.api.handlers

import com.amazonaws.services.lambda.runtime.Context

class PostLogin {
    companion object {
        class LoginData(val email: String?, val password: String?)
    }

    fun handle(input: LoginData, context: Context) {
        TODO("Fill in with database fetch")
    }
}
