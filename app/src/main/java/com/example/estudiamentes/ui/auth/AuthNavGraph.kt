package com.example.estudiamentes.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.estudiamentes.ui.setup.ProfileSetupScreen

sealed class AuthScreen(val route: String) {
    object Login : AuthScreen("login")
    object SignUp : AuthScreen("signup")
    object ForgotPassword : AuthScreen("forgot_password")
    object ResetPassword : AuthScreen("reset_password")
}

@Composable
fun AuthNavGraph(
    onAuthenticated: (String?) -> Unit,
    navController: NavHostController = rememberNavController()
) {

    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            val message = (authState as AuthState.Success).message
            if (message == "Inicio de sesión exitoso") {
                onAuthenticated(message)
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = AuthScreen.Login.route
    ) {
        composable(AuthScreen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToSignUp = { navController.navigate(AuthScreen.SignUp.route) },
                onNavigateToForgotPassword = { navController.navigate(AuthScreen.ForgotPassword.route) }
            )
        }

        composable(AuthScreen.SignUp.route) {
            SignUpScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = { navController.popBackStack() },
                onNavigateToSetup = { navController.navigate("profile_setup") }
            )
        }

        composable(AuthScreen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onNavigateToResetPassword = { navController.navigate(AuthScreen.ResetPassword.route) }
            )
        }

        composable(AuthScreen.ResetPassword.route) {
            ResetPasswordScreen(
                onNavigateToLogin = {
                    navController.navigate(AuthScreen.Login.route) {
                        popUpTo(AuthScreen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable("profile_setup") {
            ProfileSetupScreen(
                onFinish = { onAuthenticated("Registro completado") }
            )
        }
    }
}