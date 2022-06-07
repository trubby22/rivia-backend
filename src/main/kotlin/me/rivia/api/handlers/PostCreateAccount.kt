package me.rivia.api.handlers

import com.amazonaws.services.lambda.runtime.Context

class PostCreateAccount {
    companion object {
        class AccountData(
            val name: String?,
            val surname: String?,
            val email: String?,
            val password: String?
        ) {
            constructor() : this(null, null, null, null)
        }
    }

    fun handle(input: AccountData?, context: Context?) {
        TODO("Fill in with database fetch")
    }
}
