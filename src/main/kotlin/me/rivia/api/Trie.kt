package me.rivia.api

data class Trie<N : Any, L>(val children: MutableMap<N, Trie<N, L>> = HashMap(), private var alternative: Trie<N, L>? = null, private var data: L? = null) {

    operator fun get(key: List<N?>): L? = getRecursive(key.listIterator())?.data

    operator fun set(key: List<N?>, value: L) = setRecursive(key.listIterator(), value)

    private fun getRecursive(key: ListIterator<N?>, lvl: Int = 0): Trie<N, L>? =
        if (!key.hasNext()) {
            this
        } else {
            (children[key.next()] ?: alternative)?.getRecursive(key, lvl + 1)
        }

    private fun setRecursive(key: ListIterator<N?>, value: L) {
        if (!key.hasNext()) {
            data = value
            return
        }
        val entry = key.next()
        if (entry != null) {
            children.getOrPut(entry) { Trie() }.setRecursive(key, value)
            return
        }
        if (alternative == null) {
            alternative = Trie()
        }
        alternative!!.setRecursive(key, value)
    }
}
