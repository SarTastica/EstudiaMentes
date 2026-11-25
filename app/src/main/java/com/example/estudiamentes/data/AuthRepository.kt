package com.example.estudiamentes.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun register(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        firebaseAuth.createUserWithEmailAndPassword(email.trim(), password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user

                    if (user == null) {
                        firebaseAuth.signOut()
                        onResult(false, "No se ha podido obtener el usuario")
                        return@addOnCompleteListener
                    }

                    val userData = hashMapOf(
                        "correo" to email.trim(),
                        "foto" to null,
                        "perfilCompleto" to false
                    )

                    firestore.collection("users")
                        .document(user.uid)
                        .set(userData)
                        .addOnCompleteListener {
                            onResult(true, null)
                        }
                } else {
                    onResult(
                        false,
                        task.exception?.message ?: "No se ha podido crear la cuenta"
                    )
                }
            }
    }

    fun login(
        email: String,
        password: String,
        onResult: (Boolean, Boolean?, String?) -> Unit
    ) {
        firebaseAuth.signInWithEmailAndPassword(email.trim(), password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user

                    if (user == null) {
                        firebaseAuth.signOut()
                        onResult(false, null, "No se ha podido obtener el usuario.")
                        return@addOnCompleteListener
                    }

                    firestore.collection("users")
                        .document(user.uid)
                        .get()
                        .addOnSuccessListener { document ->
                            val perfilCompleto = document.getBoolean("perfilCompleto") ?: false
                            onResult(true, perfilCompleto, null)
                        }
                        .addOnFailureListener {
                            onResult(true, false, null)
                        }
                } else {
                    onResult(
                        false,
                        null,
                        task.exception?.message ?: "No se ha podido iniciar sesión"
                    )
                }
            }
    }
}