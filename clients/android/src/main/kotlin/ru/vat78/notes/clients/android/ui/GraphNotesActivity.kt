package ru.vat78.notes.clients.android.ui

import android.Manifest
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.vat78.notes.clients.android.AppEvent
import ru.vat78.notes.clients.android.AppState
import ru.vat78.notes.clients.android.data.NoteTypes
import ru.vat78.notes.clients.android.firebase.auth.FirebaseAuthentication
import ru.vat78.notes.clients.android.ui.components.PermissionDialog
import ru.vat78.notes.clients.android.ui.components.RationaleDialog
import ru.vat78.notes.clients.android.ui.theme.GraphNotesTheme

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
class GraphNotesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(
            ComposeView(this).apply {
                setContent {
                    GraphNotesApp()
                }
            }
        )
    }
}

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
@Composable
fun GraphNotesApp() {
    GraphNotesTheme {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            RequestNotificationPermissionDialog()
        }

        val appState = rememberAppState()
        appState.context.services.init(LocalContext.current)
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val userLoginScreen = remember { mutableStateOf(false) }
        val currentUser = appState.context.currentUser.collectAsState(null)

        // Intercepts back navigation when the drawer is open
        if (drawerState.isOpen) {
            BackHandler {
                scope.launch {
                    drawerState.close()
                }
            }
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                DrawerNavigationMenu({ NoteTypes.types.values }) {
                    appState.navigate(it)
                    Log.i("DrawerNavigationMenu", "Selected $it")
                    scope.launch {
                        drawerState.close()
                    }
                }
            }
        ) {
            if (userLoginScreen.value) {
                userLoginScreen.value = false
                FirebaseAuthentication(
                    onSignIn = { appState.context.riseEvent(AppEvent.OnAuth(it)) },
                    onFailure = { appState.context.riseEvent(AppEvent.OnAuth(null)) },
                )
            }

            if (currentUser.value != null) {
                GraphNotesNavHost(
                    appState = appState,
                    modifier = Modifier.padding(8.dp),
                    onNavIconPressed = {
                        scope.launch {
                            drawerState.open()
                        }
                    }
                )
            }
        }
        LaunchedEffect(appState) {
            userLoginScreen.value = true
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestNotificationPermissionDialog() {
    val permissionState = rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)

    if (!permissionState.status.isGranted) {
        if (permissionState.status.shouldShowRationale) RationaleDialog()
        else PermissionDialog { permissionState.launchPermissionRequest() }
    }
}

@Composable
fun rememberAppState(
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    navController: NavHostController = rememberNavController(),
    resources: Resources = resources(),
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) =
    remember(snackbarHostState, navController, resources, coroutineScope) {
        AppState(snackbarHostState, navController, resources, coroutineScope)
    }

@Composable
@ReadOnlyComposable
fun resources(): Resources {
    LocalConfiguration.current
    return LocalContext.current.resources
}

