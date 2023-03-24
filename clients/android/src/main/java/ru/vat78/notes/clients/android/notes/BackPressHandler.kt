package ru.vat78.notes.clients.android.notes

import androidx.activity.OnBackPressedDispatcher
import androidx.compose.runtime.staticCompositionLocalOf

val LocalBackPressedDispatcher =
    staticCompositionLocalOf<OnBackPressedDispatcher> { error("No Back Dispatcher provided") }