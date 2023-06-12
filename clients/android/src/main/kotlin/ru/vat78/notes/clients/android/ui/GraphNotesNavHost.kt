package ru.vat78.notes.clients.android.ui

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import ru.vat78.notes.clients.android.AppState
import ru.vat78.notes.clients.android.data.Note
import ru.vat78.notes.clients.android.ui.screens.editor.NoteEditor
import ru.vat78.notes.clients.android.ui.screens.notes.TagNotes
import ru.vat78.notes.clients.android.ui.screens.tags.Tags
import ru.vat78.notes.clients.android.ui.screens.timeline.TimeLineScreen

@Composable
@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
@ExperimentalFoundationApi
fun GraphNotesNavHost(
    appState: AppState,
    modifier: Modifier = Modifier,
    onNavIconPressed: () -> Unit = { },
) {
    NavHost(
        navController = appState.navController,
        startDestination = NoteListScreen.route,
        modifier = modifier
    ) {

        val toNoteEditAction: (Note) -> Unit = { note ->
            appState.navigate(
                route = "${EditNoteScreen.route}/${note.id}"
            )
        }
        val toNewNoteEditAction: () -> Unit = {
            appState.navigate(
                route = "${EditNoteScreen.route}/new"
            )
        }

        composable(route = NoteListScreen.route) {
            Log.i("GraphNotesNavHost", "Route to ${NoteListScreen.route}")
            TimeLineScreen(
                appState = appState,
                onNoteClick = toNoteEditAction,
                onCreateNote = toNewNoteEditAction,
                onNavIconPressed = onNavIconPressed
            )
        }
        composable(
            route = EditNoteScreen.routeWithArgs,
            arguments = listOf(
                navArgument(EditNoteScreen.uuidArgument) {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) { entry ->
            val noteUuid = entry.arguments?.getString(EditNoteScreen.uuidArgument)
            Log.i("GraphNotesNavHost", "Route to ${EditNoteScreen.route} with uuid = $noteUuid")
            NoteEditor(
                noteUuid = noteUuid ?: "",
                appState = appState,
                onExit = { appState.popUp()}
            )
        }
        composable(
            route = TagListScreen.routeWithArgs,
            arguments = listOf(
                navArgument(TagListScreen.tagType) {
                    type = NavType.StringType
                    nullable = false
                },
                navArgument(TagListScreen.rootTag) {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { entry ->
            val type = entry.arguments?.getString(TagListScreen.tagType)
            val rootId  = entry.arguments?.getString(TagListScreen.rootTag)
            Log.i("GraphNotesNavHost", "Route to ${TagListScreen.route} with type = $type and root = $rootId")
            Tags(
                type = type ?: "",
                rootId  = rootId,
                appState = appState,
                onNoteClick = { note ->
                    appState.navigate(
                        route = "${TagListScreen.route}/${note.type}?root=${note.id}"
                    )
                },
                onCreateNote = toNewNoteEditAction,
                onCaptionClick = toNoteEditAction,
                onNavIconPressed = onNavIconPressed
            )
        }

        composable(
            route = TagNotesScreen.routeWithArgs,
            arguments = listOf(
                navArgument(TagNotesScreen.rootTag) {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { entry ->
            val rootId  = entry.arguments?.getString(TagNotesScreen.rootTag)
            Log.i("GraphNotesNavHost", "Route to ${TagNotesScreen.route} with root = $rootId")
            TagNotes(
                rootId  = rootId!!,
                appState = appState,
                onNoteClick = toNoteEditAction,
                onCreateNote = toNewNoteEditAction,
                onCaptionClick = toNoteEditAction,
                onNavIconPressed = onNavIconPressed
            )
        }
    }
}
