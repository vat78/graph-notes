package ru.vat78.notes.clients.android.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.navArgument

interface BaseScreen {
    val icon: ImageVector
    val route: String
}

val appBaseScreens = listOf(NoteListScreen)

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

object NoteListScreen: BaseScreen {
    override val icon = Icons.Filled.Check
    override val route = "notes"
}

object EditNoteScreen: BaseScreen {
    override val icon = Icons.Filled.Edit
    override val route = "edit_note"
    const val uuidArgument = "uuid"
    val routeWithArgs = "${route}/{${uuidArgument}}"
}

object TagListScreen: BaseScreen {
    override val icon = Icons.Filled.Edit
    override val route = "tags"
    const val tagType = "type"
    const val rootTag = "root"
    val routeWithArgs = "${route}/{${tagType}}?root={${rootTag}}"
}