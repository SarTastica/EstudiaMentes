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

    //Esto es principalmete para mostrar o no mostrar las paginas de configuracion la primera vez que el usuario inicia sesion
    LaunchedEffect(authState) {
        val state = authState

        if (state is AuthState.Success) {

            if (state.perfilCompleto == false) {

                // El usuario todavía no ha configurado su perfil
                navController.navigate("profile_setup") {
                    popUpTo(AuthScreen.Login.route) {
                        inclusive = true
                    }
                }

            } else if (state.perfilCompleto == true) {

                // El usuario ya tiene su perfil configurado
                onAuthenticated(state.message)
            }

            authViewModel.consumeSuccess()
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
                onNavigateToLogin = {
                    authViewModel.resetState()
                    navController.popBackStack()
                },
                onNavigateToSetup = {
                    // La configuración se hará después de verificar
                    // el correo e iniciar sesión.
                }
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