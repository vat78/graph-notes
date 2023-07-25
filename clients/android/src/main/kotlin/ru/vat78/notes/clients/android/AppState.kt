package ru.vat78.notes.clients.android

import android.content.res.Resources
import androidx.compose.material3.SnackbarHostState
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope

class AppState(
    val snackbarHostState: SnackbarHostState,
    val navController: NavHostController,
    private val resources: Resources,
    val coroutineScope: CoroutineScope,
    val context:ApplicationContext = ApplicationContext()
) {

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