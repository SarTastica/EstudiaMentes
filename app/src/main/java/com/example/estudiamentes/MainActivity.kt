package com.example.estudiamentes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.estudiamentes.ui.auth.AuthNavGraph
import com.example.estudiamentes.ui.navigation.AppNavigation // Importamos tu nuevo enrutador global
import com.example.estudiamentes.ui.theme.EstudiaMentesTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            EstudiaMentesTheme {

                var isAuthenticated by remember {
                    mutableStateOf(
                        FirebaseAuth.getInstance().currentUser != null
                    )
                }

                var welcomeMessage by remember {
                    mutableStateOf<String?>(null)
                }

                if (isAuthenticated) {
                    AppNavigation()
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