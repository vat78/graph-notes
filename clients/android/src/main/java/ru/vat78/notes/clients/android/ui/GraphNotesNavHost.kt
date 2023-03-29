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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ru.vat78.notes.clients.android.data.TaskShortView
import ru.vat78.notes.clients.android.notes.NoteListContent
import ru.vat78.notes.clients.android.notes.NotesViewModel
import ru.vat78.notes.clients.android.tasks.SingleTaskScreen
import ru.vat78.notes.clients.android.tasks.TasksScreen

@Composable
fun GraphNotesNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NoteListScreen.route,
        modifier = modifier
    ) {
        composable(route = NoteListScreen.route) {
            NoteListContent(
                viewModel = NotesViewModel(),
            )
        }
        composable(route = Tasks.route) {
            TasksScreen(
                onTaskClick = { task ->
                    navController.navigateToSingleTask(task)
                }
            )
        }
        composable(
            route = SingleTask.routeWithArgs,
            arguments = SingleTask.arguments
        ) { navBackStackEntry ->
            val taskUuid =
                navBackStackEntry.arguments?.getString(SingleTask.uuid)
            SingleTaskScreen(
                taskUuid.orEmpty(),
                onClose = {navController.popBackStack()}
            )
        }
    }
}

fun NavHostController.navigateSingleTopTo(route: String) =
    this.navigate(route) {
        // Pop up to the start destination of the graph to
        // avoid building up a large stack of destinations
        // on the back stack as users select items
        popUpTo(
            this@navigateSingleTopTo.graph.findStartDestination().id
        ) {
            saveState = true
        }
        // Avoid multiple copies of the same destination when
        // reselecting the same item
        launchSingleTop = true
        // Restore state when reselecting a previously selected item
        restoreState = true
    }

private fun NavHostController.navigateToSingleTask(task: TaskShortView) {
    this.navigateSingleTopTo("${SingleTask.route}/${task.uuid}")
}
