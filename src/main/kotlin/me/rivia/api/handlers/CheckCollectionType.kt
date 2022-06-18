package me.rivia.api.handlers

inline fun <reified I> List<*>.checkListType(): List<I>? {
    for (item in this) {
        if (item !is I) {
            return null
        }
    }
    return this as List<I>
}
