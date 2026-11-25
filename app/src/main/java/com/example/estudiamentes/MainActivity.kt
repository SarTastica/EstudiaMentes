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

                val auth = remember { FirebaseAuth.getInstance() }
                var isAuthenticated by remember { mutableStateOf(false) }
                var isCheckingSession by remember { mutableStateOf(true) }
                var isRegistering by remember { mutableStateOf(false) }
                var welcomeMessage by remember { mutableStateOf<String?>(null) }

                DisposableEffect(auth) {
                    val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                        val user = firebaseAuth.currentUser

                        if (user == null) {
                            isAuthenticated = false
                            isCheckingSession = false
                        } else if (!isRegistering) {
                            user.reload().addOnCompleteListener { task ->
                                val updatedUser = firebaseAuth.currentUser

                                if (task.isSuccessful && updatedUser != null) {
                                    isAuthenticated = true
                                } else if (task.exception is FirebaseAuthInvalidUserException) {
                                    firebaseAuth.signOut()
                                    isAuthenticated = false
                                } else {
                                    isAuthenticated = updatedUser != null
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
                            isRegistering = false
                            auth.signOut()
                        }
                    )
                } else {
                    AuthNavGraph(
                        onStartRegister = {
                            isRegistering = true
                        },
                        onAuthenticated = { message ->
                            welcomeMessage = message
                            isRegistering = false
                            isAuthenticated = true
                        }
                    )
                }
            }
        }
    }
}