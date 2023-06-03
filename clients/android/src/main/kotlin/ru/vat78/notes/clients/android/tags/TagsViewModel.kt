package ru.vat78.notes.clients.android.tags

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import ru.vat78.notes.clients.android.ApplicationContext
import ru.vat78.notes.clients.android.base.BaseViewModel
import ru.vat78.notes.clients.android.notes.ListState

class TagsViewModel(
    contextHolder: ApplicationContext,
) : BaseViewModel<TagsUiState, TagsUiEvent>() {

    private val reducer = TagsUiReducer(
        initial = TagsUiState(
            state = ListState.INIT
        ),
        contextHolder = contextHolder,
        viewModelScope = viewModelScope
    )

    override val state: StateFlow<TagsUiState>
        get() = reducer.state

    fun sendEvent(event: TagsUiEvent) {
        reducer.sendEvent(event)
    }
}