
package com.example.estudiamentes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.estudiamentes.ui.auth.AuthNavGraph
import com.example.estudiamentes.ui.navigation.AppNavigation
import com.example.estudiamentes.ui.theme.EstudiaMentesTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            EstudiaMentesTheme {

                val auth = remember {
                    FirebaseAuth.getInstance()
                }

                var isAuthenticated by remember {
                    mutableStateOf(false)
                }

                var isCheckingSession by remember {
                    mutableStateOf(true)
                }

                var welcomeMessage by remember {
                    mutableStateOf<String?>(null)
                }

                DisposableEffect(auth) {

                    val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->

                        val user = firebaseAuth.currentUser

                        if (user == null) {
                            isAuthenticated = false
                            isCheckingSession = false
                        } else {
                            // Comprobamos que la cuenta sigue siendo válida.
                            user.reload().addOnCompleteListener { task ->

                                val updatedUser = firebaseAuth.currentUser

                                if (
                                    task.isSuccessful &&
                                    updatedUser != null &&
                                    updatedUser.isEmailVerified
                                ) {
                                    isAuthenticated = true

                                } else if (
                                    task.exception is FirebaseAuthInvalidUserException ||
                                    (task.isSuccessful && updatedUser?.isEmailVerified != true)
                                ) {
                                    firebaseAuth.signOut()
                                    isAuthenticated = false

                                } else {
                                    // Si falla la conexión, mantenemos la sesión
                                    // previamente verificada.
                                    isAuthenticated =
                                        updatedUser?.isEmailVerified == true
                                }

                                isCheckingSession = false
                            }
                        }
                    }

                    auth.addAuthStateListener(listener)

                    onDispose {
                        auth.removeAuthStateListener(listener)
                    }
                }

                if (isCheckingSession) {

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }

                } else if (isAuthenticated) {

                    AppNavigation(
                        onLogout = {
                            auth.signOut()
                        }
                    )

                } else {

                    AuthNavGraph(
                        onAuthenticated = { message ->
                            welcomeMessage = message
                            isAuthenticated = true
                        }
                    )
                }
            }
        }
    }
}
