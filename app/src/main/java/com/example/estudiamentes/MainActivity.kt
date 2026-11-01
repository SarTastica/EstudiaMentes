package com.example.estudiamentes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.estudiamentes.ui.auth.AuthNavGraph
import com.example.estudiamentes.ui.theme.EstudiaMentesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EstudiaMentesTheme {
                AuthNavGraph()
            }
        }
    }
}