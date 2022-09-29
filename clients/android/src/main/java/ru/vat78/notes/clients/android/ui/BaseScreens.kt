package ru.vat78.notes.clients.android.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.navArgument

interface BaseScreen {
    val icon: ImageVector
    val route: String
}

val appBaseScreens = listOf(Tasks)

object Tasks: BaseScreen {
    override val icon = Icons.Filled.Check
    override val route = "tasks"
}

object SingleTask: BaseScreen {
    override val icon = Icons.Filled.Check
    override val route = "task"
    const val uuid = "task_uuid"
    val routeWithArgs = "$route/{$uuid}"
    val arguments = listOf(
        navArgument(uuid) { type = NavType.StringType}
    )
}