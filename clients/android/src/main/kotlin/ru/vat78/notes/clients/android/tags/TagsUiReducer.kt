package ru.vat78.notes.clients.android.tags

import kotlinx.coroutines.CoroutineScope
import ru.vat78.notes.clients.android.ApplicationContext
import ru.vat78.notes.clients.android.base.Reducer

class TagsUiReducer (
    initial: TagsUiState,
    val contextHolder: ApplicationContext,
    val viewModelScope: CoroutineScope
) : Reducer<TagsUiState, TagsUiEvent>(initial) {

    override fun reduce(oldState: TagsUiState, event: TagsUiEvent) {
        TODO("Not yet implemented")
    }
}