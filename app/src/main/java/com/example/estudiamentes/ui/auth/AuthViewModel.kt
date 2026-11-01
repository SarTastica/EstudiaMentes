package com.example.estudiamentes.ui.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(email: String, password: String) {
        if (!validateEmailAndPassword(email, password)) return

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            _authState.value = AuthState.Success("Inicio de sesión exitoso")
        }
    }

    fun signUp(email: String, password: String, confirmPassword: String) {
        if (!validateEmailAndPassword(email, password)) return

        if (password != confirmPassword) {
            _authState.value = AuthState.Error("Las contraseñas no coinciden")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            _authState.value = AuthState.Success("Registro completado con éxito")
        }
    }

    fun resetPassword(email: String) {
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authState.value = AuthState.Error("Introduce un correo electrónico válido")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            _authState.value = AuthState.Success("Enlace enviado a tu correo")
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    private fun validateEmailAndPassword(email: String, password: String): Boolean {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Por favor, rellena todos los campos")
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authState.value = AuthState.Error("El formato del correo no es válido")
            return false
        }
        if (password.length < 6) {
            _authState.value = AuthState.Error("La contraseña debe tener al menos 6 caracteres")
            return false
        }
        return true
    }
}