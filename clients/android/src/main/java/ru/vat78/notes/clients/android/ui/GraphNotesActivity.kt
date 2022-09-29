package ru.vat78.notes.clients.android.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ru.vat78.notes.clients.android.ui.components.MainTabRow
import ru.vat78.notes.clients.android.ui.theme.GraphNotesTheme

class GraphNotesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GraphNotesApp()
        }
    }
}

@Composable
fun GraphNotesApp() {
    GraphNotesTheme {
        val navController = rememberNavController()
        val currentBackStack by navController.currentBackStackEntryAsState()
        val currentDestination = currentBackStack?.destination
        var currentScreen = appBaseScreens.find { it.route == currentDestination?.route } ?: Tasks
        Scaffold(
            topBar = {
                MainTabRow(
                    allScreens = appBaseScreens,
                    onTabSelected = { newScreen ->
                        navController.navigateSingleTopTo(newScreen.route) },
                    currentScreen = currentScreen
                )
            }
        ) { innerPadding ->
            GraphNotesNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}