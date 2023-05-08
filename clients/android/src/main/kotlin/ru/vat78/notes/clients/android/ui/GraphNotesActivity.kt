@file:OptIn(ExperimentalMaterial3Api::class,ExperimentalFoundationApi::class,ExperimentalComposeUiApi::class)

package ru.vat78.notes.clients.android.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import ru.vat78.notes.clients.android.data.NotesStorage
import ru.vat78.notes.clients.android.ui.theme.GraphNotesTheme

class GraphNotesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(
            ComposeView(this).apply {
                setContent {
                    GraphNotesApp()
                }
            }
        )
    }
}

@Composable
fun GraphNotesApp() {
    GraphNotesTheme {

        val navController = rememberNavController()
        Scaffold { innerPadding ->
            GraphNotesNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
                noteStorage = NotesStorage()
            )
        }
    }
}