
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
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    val user = task.result?.user

                    if (user == null) {
                        firebaseAuth.signOut()
                        onResult(false, "No se ha podido obtener el usuario")
                        return@addOnCompleteListener
                    }

                    // Crear el documento inicial del usuario en Firestore.
                    val userData = hashMapOf(
                        "correo" to email,
                        "foto" to null,
                        "perfilCompleto" to false
                    )

                    firestore.collection("users")
                        .document(user.uid)
                        .set(userData)
                        .addOnSuccessListener {

                            // Enviar correo de verificación.
                            user.sendEmailVerification()
                                .addOnCompleteListener { verificationTask ->

                                    firebaseAuth.signOut()

                                    if (verificationTask.isSuccessful) {
                                        onResult(true, null)
                                    } else {
                                        onResult(
                                            false,
                                            "No se ha podido enviar el correo de verificación"
                                        )
                                    }
                                }
                        }
                        .addOnFailureListener { exception ->
                            firebaseAuth.signOut()
                            onResult(
                                false,
                                "Error al guardar el usuario: ${exception.message}"
                            )
                        }

                } else {
                    onResult(
                        false,
                        task.exception?.message
                            ?: "No se ha podido crear la cuenta"
                    )
                }
            }
    }

    fun login(
        email: String,
        password: String,
        onResult: (Boolean, Boolean?, String?) -> Unit
    ) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    val user = task.result?.user

                    if (user == null) {
                        firebaseAuth.signOut()
                        onResult(
                            false,
                            null,
                            "No se ha podido obtener el usuario."
                        )
                        return@addOnCompleteListener
                    }

                    // Actualizar los datos del usuario desde Firebase.
                    user.reload()
                        .addOnCompleteListener { reloadTask ->

                            if (!reloadTask.isSuccessful) {
                                firebaseAuth.signOut()
                                onResult(
                                    false,
                                    null,
                                    "No se ha podido comprobar tu cuenta."
                                )
                                return@addOnCompleteListener
                            }

                            // Comprobar que el correo está verificado.
                            if (user.isEmailVerified != true) {
                                firebaseAuth.signOut()
                                onResult(
                                    false,
                                    null,
                                    "Debes verificar tu correo electrónico antes de iniciar sesión."
                                )
                                return@addOnCompleteListener
                            }

                            // Consultar si el perfil está completo.
                            firestore.collection("users")
                                .document(user.uid)
                                .get()
                                .addOnSuccessListener { document ->

                                    val perfilCompleto =
                                        document.getBoolean("perfilCompleto")
                                            ?: false

                                    onResult(
                                        true,
                                        perfilCompleto,
                                        null
                                    )
                                }
                                .addOnFailureListener { exception ->
                                    firebaseAuth.signOut()
                                    onResult(
                                        false,
                                        null,
                                        "Error al consultar tu perfil: ${exception.message}"
                                    )
                                }
                        }

                } else {
                    onResult(
                        false,
                        null,
                        task.exception?.message
                            ?: "No se ha podido iniciar sesión"
                    )
                }
            }
    }
}
