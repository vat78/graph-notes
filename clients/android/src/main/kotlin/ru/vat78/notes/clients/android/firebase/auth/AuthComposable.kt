package ru.vat78.notes.clients.android.firebase.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.AuthUI.IdpConfig.EmailBuilder
import com.firebase.ui.auth.AuthUI.IdpConfig.GitHubBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import ru.vat78.notes.clients.android.R
import ru.vat78.notes.clients.android.data.User

@Composable
fun FirebaseAuthentication(
    onSignIn: (User?) -> Unit = {},
    onFailure: () -> Unit = {},
    firebaseAuth: FirebaseAuth = Firebase.auth
) {

    val currentUser = firebaseAuth.currentUser
    if (currentUser != null) {
        onSignIn.invoke(currentUser.toAppUser())
        return
    }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val responseId = result.resultCode
        if (responseId == Activity.RESULT_OK) {
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser == null) {
                onSignIn.invoke(null)
            } else {
                onSignIn.invoke(firebaseUser.toAppUser())
            }
        } else {
            onFailure.invoke()
        }
    }

    LaunchedEffect(Unit) {
        signInLauncher.launch(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(
                    listOf(
                        EmailBuilder().build(),
                        GitHubBuilder().build()
                    )
                )
                .setTheme(R.style.Theme_AppCompat_Light)
                .setLogo(R.drawable.ic_launcher_background)
                .setAlwaysShowSignInMethodScreen(true)
                .build()
        )
    }
}

private fun FirebaseUser.toAppUser() : User {
    val emailTxt = email ?: ""
    return User(
        id = uid,
        name = displayName ?: emailTxt,
        email = emailTxt
    )
}