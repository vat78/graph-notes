/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.vat78.notes.clients.android.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import ru.vat78.notes.clients.android.AppState
import ru.vat78.notes.clients.android.notes.NoteEditor
import ru.vat78.notes.clients.android.notes.NoteEditorViewModel
import ru.vat78.notes.clients.android.notes.NoteListContent
import ru.vat78.notes.clients.android.notes.NotesViewModel
import ru.vat78.notes.clients.android.tags.TagListContent
import ru.vat78.notes.clients.android.tags.TagsViewModel

@Composable
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
        composable(route = NoteListScreen.route) {
            NoteListContent(
                viewModel = NotesViewModel(appState.context),
                onNoteClick = { noteUuid ->
                    appState.navigate(
                        route = "${EditNoteScreen.route}/${noteUuid}"
                    )
                },
                onCreateNote = {
                    appState.navigate(
                        route = "${EditNoteScreen.route}/new"
                    )
                },
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
            NoteEditor(
                noteUuid = entry.arguments?.getString(EditNoteScreen.uuidArgument) ?: "",
                viewModel = NoteEditorViewModel(appState.context),
                onExit = { appState.popUp()}
            )
        }
        composable(
            route = TagListScreen.routeWithArgs,
            arguments = listOf(
                navArgument(TagListScreen.tagType) {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) { entry ->
            TagListContent(
                type = entry.arguments?.getString(TagListScreen.tagType) ?: "",
                viewModel = TagsViewModel(appState.context),
            )
        }

    }
}
