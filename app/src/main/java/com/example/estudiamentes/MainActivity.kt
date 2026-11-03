package com.example.estudiamentes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.example.estudiamentes.ui.MainNavGraph
import com.example.estudiamentes.ui.auth.AuthNavGraph
import com.example.estudiamentes.ui.theme.EstudiaMentesTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            EstudiaMentesTheme {

                //Cuando isAuthenticated sea true, se actualizará la interfaz
                var isAuthenticated by remember {
                    mutableStateOf(
                        FirebaseAuth.getInstance().currentUser != null
                    )
                }

                Text("Autenticado: $isAuthenticated")

                var welcomeMessage by remember {
                    mutableStateOf<String?>(null)
                }

                //Si el usuario esta autenticado, se mutilizaría en MainNav, si no el AuthNav
                if (isAuthenticated) {
                    MainNavGraph(
                        welcomeMessage = welcomeMessage,
                        onLogout = {
                            FirebaseAuth.getInstance().signOut()
                            isAuthenticated = false
                            welcomeMessage = null
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