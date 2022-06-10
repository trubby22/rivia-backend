package me.rivia.api.handlers

import me.rivia.api.database.initDb

open class HandlerInit {
    init {
        initDb()
    }
}
