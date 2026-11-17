
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
    object ProfileSetup : AuthScreen("profile_setup")
}

@Composable
fun AuthNavGraph(
    onAuthenticated: (String?) -> Unit,
    navController: NavHostController = rememberNavController()
) {
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()

    // Decide si el usuario necesita configurar su perfil.
    LaunchedEffect(authState) {
        val state = authState

        if (state is AuthState.Success) {

            if (state.message == "Inicio de sesión exitoso") {

                if (state.perfilCompleto == false) {
                    navController.navigate(AuthScreen.ProfileSetup.route) {
                        popUpTo(AuthScreen.Login.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                } else if (state.perfilCompleto == true) {
                    onAuthenticated(state.message)
                }
            }

            // Evita que el mismo éxito vuelva a procesarse.
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
                onNavigateToSignUp = {
                    navController.navigate(AuthScreen.SignUp.route)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(AuthScreen.ForgotPassword.route)
                }
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
                    // El perfil se configura después de verificar
                    // el correo e iniciar sesión.
                }
            )
        }

        composable(AuthScreen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onNavigateToResetPassword = {
                    navController.navigate(AuthScreen.ResetPassword.route)
                }
            )
        }

        composable(AuthScreen.ResetPassword.route) {
            ResetPasswordScreen(
                onNavigateToLogin = {
                    navController.navigate(AuthScreen.Login.route) {
                        popUpTo(AuthScreen.Login.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(AuthScreen.ProfileSetup.route) {
            ProfileSetupScreen(
                onFinish = {
                    onAuthenticated("Registro completado")
                }
            )
        }
    }
}
