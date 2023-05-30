package ru.vat78.notes.clients.android.data

fun buildSearchBlocks(text: String): Set<String> {
    val words = getWordsForSearch(text)
    return words.asSequence()
        .flatMap { splitWordOnTokens(it) }
        .toSet()
}

fun getWordsForSearch(text: String): Set<String> {
    val regex = Regex("[^\\p{L}\\p{Nd}]+")
    return text.split(regex).asSequence().filter { it.length >= 2 }.toSet()
}

private fun splitWordOnTokens(word: String) : Sequence<String> {
    return if (word.length < 2) return emptySequence()
    else IntRange(2, word.length).asSequence().map { word.substring(0, it) }
}