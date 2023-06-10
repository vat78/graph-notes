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
import ru.vat78.notes.clients.android.ui.screens.editor.NoteEditor
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
        composable(route = NoteListScreen.route) {
            Log.i("GraphNotesNavHost", "Route to ${NoteListScreen.route}")
            TimeLineScreen(
                appState = appState,
                onNoteClick = { note ->
                    appState.navigate(
                        route = "${EditNoteScreen.route}/${note.id}"
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
                onCreateNote = {
                    appState.navigate(
                        route = "${EditNoteScreen.route}/new"
                    )
                },
                onCaptionClick = { note ->
                    appState.navigate(
                        route = "${EditNoteScreen.route}/${note.id}"
                    )
                },
                onNavIconPressed = onNavIconPressed
            )
        }

    }
}
