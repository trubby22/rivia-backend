package me.rivia.api.handlers

import com.amazonaws.services.lambda.runtime.Context


class PostCreateAccount {
    companion object {
        class AccountData(
            var name: String?,
            var surname: String?,
            var email: String?,
            var password: String?
        ) {
            constructor() : this(null, null, null, null)
        }
    }

    fun handle(input: AccountData?, context: Context?) {
        TODO("Fill in")
    }
}
