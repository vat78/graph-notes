package ru.vat78.notes.clients.android.notes

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import ru.vat78.notes.clients.android.base.BaseViewModel
import ru.vat78.notes.clients.android.data.Note
import java.time.LocalDateTime

class NotesViewModel() : BaseViewModel<NotesUiState, NotesUiEvent>() {

    private val reducer = NotesUiReducer(
        initial = NotesUiState(
            caption = "Test",
            notes = listOf(
                Note(caption = "test 1"),
                Note(caption = "test 2"),
                Note(caption = "test formatted", start = LocalDateTime.of(2023, 3, 23, 18, 55), description = "shfsh ;ljrg sdfbgsn @test fl;cvbmvcx fdsdfblmkb sbksdlkb sbnlskdgb dsklbnlksdbn  sdfbldskblksd dsbkmdsblksm sdfkblmskldbm sdbmsldkbm  sdmbflkdsmbsl dsbmlkdsmb sdbklmsldkbms sdmflbsld"),
                Note(caption = "test 3", start = LocalDateTime.of(2023, 3, 22, 18, 55), description = "dlfak *vblmafbvmf afbmafdbma* abfdbbadabd"),
                Note(caption = "test 4", start = LocalDateTime.of(2023, 3, 22, 18, 0))
            )
        ),
        viewModelScope = viewModelScope
    )

    override val state: StateFlow<NotesUiState>
        get() = reducer.state

    fun sendEvent(event: NotesUiEvent) {
        reducer.sendEvent(event)
    }
}