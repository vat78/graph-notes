package ru.vat78.notes.clients.android

import android.content.res.Resources
import androidx.compose.material3.SnackbarHostState
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import ru.vat78.notes.clients.android.data.GlobalEventHandler

class AppState(
    val snackbarHostState: SnackbarHostState,
    val navController: NavHostController,
    private val resources: Resources,
    private val coroutineScope: CoroutineScope,
    val context:ApplicationContext = ApplicationContext()
) {

    init {
        GlobalEventHandler.init()
    }

    fun popUp() {
        navController.popBackStack()
    }

    fun navigate(route: String) {
        navController.navigate(route) { launchSingleTop = false }
    }

    fun navigateAndPopUp(route: String, popUp: String) {
        navController.navigate(route) {
            launchSingleTop = true
            popUpTo(popUp) { inclusive = true }
        }
    }

    fun clearAndNavigate(route: String) {
        navController.navigate(route) {
            launchSingleTop = true
            popUpTo(0) { inclusive = true }
        }
    }
}