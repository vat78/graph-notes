package ru.vat78.notes.clients.android.data

import android.util.Log
import androidx.compose.ui.text.input.TextFieldValue
import ru.vat78.notes.clients.android.ui.ext.analyzeTags


interface WordStorage {
    suspend fun findTagIdsByWords(words: Collection<String>): List<String>
    suspend fun findTagIdsByWordsAndType(words: Collection<String>, tagType: String): Set<String>
    suspend fun saveOrUpdate(words: Collection<String>): List<Long>
    suspend fun insertForTag(wordIds: Collection<Long>, noteId: String, typeId: String)
    suspend fun deleteAllByTagId(tagId: String)
}

suspend fun searchTagsByCaption(text: String, type: NoteType, parent: DictionaryElement?, wordStorage: WordStorage,
                                linkStorage: NoteLinkStorage, tagStorage: TagStorage): Set<DictionaryElement> {
    Log.i("Suggestions", "Search suggestions for simple text $text and tag type $type")
    val words = getWordsForSearch(text)
    val tagIds = wordStorage.findTagIdsByWordsAndType(words, type.id)
    val filteredIds = if (parent == null) {
        tagIds
    } else {
        val childrenIds = linkStorage.getChildrenIds(listOf(parent.id), listOf(type.id))
        childrenIds.intersect(tagIds)
    }
    if (filteredIds.isEmpty()) {
        return emptySet();
    }
    return tagStorage.getTagsByIds(filteredIds)
}

suspend fun searchTagSuggestions(text: String, note: NoteWithParents, maxCount: Int,
                                 wordStorage: WordStorage, tagStorage: TagStorage): List<DictionaryElement> {
    Log.i("Suggestions", "Search suggestions for tag text $text")
    val excludedTypes = note.parents
        .filter { it.type.hierarchical }
        .map { it.type.id }
        .toSet()
    val excludedTags = note.parents.map { it.id }.toSet() + note.note.id

    val selectedType = getTypeByFirstSymbol(text)
    if (selectedType == null) {
        val clearedText = text.substring(1)
        val words = getWordsForSearch(clearedText)
        val tagIds = wordStorage.findTagIdsByWords(words)
        val filteredIds = tagIds - excludedTags
        val result = if (filteredIds.isEmpty()) { emptyList() } else { tagStorage.getTagsByIds(filteredIds.take(maxCount)) }
        return result + newDictionaryElementForSuggestion('#', clearedText)
    } else {
        if (excludedTypes.contains(selectedType.id)) {
            return emptyList()
        }
        val clearedText = text.substring(1)
        val words = getWordsForSearch(clearedText)
        val tagIds = wordStorage.findTagIdsByWordsAndType(words, selectedType.id)
        val filteredIds = tagIds - excludedTags
        val result = if (filteredIds.isEmpty()) { emptyList() } else { tagStorage.getTagsByIds(filteredIds.take(maxCount)) }
        return result + newDictionaryElementForSuggestion(selectedType.symbol, clearedText)
    }

}

private fun newDictionaryElementForSuggestion(tagSymbol: Char, tagText: String) : DictionaryElement {
    val tagType = NoteTypes.types.values.first { it.symbol == tagSymbol }
    return DictionaryElement(
        id = "",
        type = tagType,
        caption = tagText
    )
}

private fun buildSearchBlocks(text: String): Set<String> {
    val words = getWordsForSearch(text)
    return words.asSequence()
        .map { it.trim('(', ',', '.', '+', ')', '!', '?', '#') }
        .flatMap { splitWordOnTokens(it) }
        .toSet()
}

private fun getWordsForSearch(text: String): Set<String> {
    val regex = Regex("[^\\p{L}\\p{Nd}]+")
    return text.split(regex).asSequence().filter { it.length >= 2 }.map { it.lowercase()}.toSet()
}

private fun splitWordOnTokens(word: String) : Sequence<String> {
    return if (word.length < 2) return emptySequence()
    else IntRange(2, word.length).asSequence().map { word.substring(0, it) }.map { it.lowercase() }
}

data class ValidateNoteSuggestionEvent(
    val notes: List<Note>,
    val noteStorage: NoteStorage,
    val tagStorage: TagStorage,
    val onUpdate: (List<Note>) -> Unit = {}
): ApplicationEvent {
    override suspend fun handle() {
        val tagIds = notes.asSequence().flatMap { it.textInsertions.keys.asSequence() }.toSet()
        val tags = tagStorage.getTagsByIds(tagIds).associateBy { it.id }
        val notesForUpdate = mutableListOf<Note>()
        notes.forEach { note ->
            var changed = false
            val newInsertions = mutableMapOf<String, DictionaryElement>()
            note.textInsertions.entries.forEach {
                val tag = tags[it.key]
                if (tag == null || tag.caption == it.value.caption) {
                    newInsertions[it.key] = it.value
                } else {
                    newInsertions[it.key] = tag
                    changed = true
                }
            }
            if (changed) {
                notesForUpdate.add(note.copy(textInsertions = newInsertions))
            }
        }
        noteStorage.updateSuggestions(notesForUpdate)
        onUpdate.invoke(notesForUpdate)
    }
}

data class UpdateTagWordsEvent(
    val note: Note,
    val wordStorage: WordStorage
): ApplicationEvent {
    override suspend fun handle() {
        wordStorage.deleteAllByTagId(note.id)
        if (note.deleted) {
            return
        }
        val textBlocks = buildSearchBlocks(note.caption)
        val ids = wordStorage.saveOrUpdate(textBlocks)
        wordStorage.insertForTag(ids, note.id, note.type.id)
    }
}