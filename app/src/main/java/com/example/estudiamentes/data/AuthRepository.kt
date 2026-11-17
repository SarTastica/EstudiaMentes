
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
                        onResult(false, "No se ha podido obtener el usuario")
                        return@addOnCompleteListener
                    }

                    // Datos básicos del usuario
                    val userData = hashMapOf(
                        "correo" to email,
                        "foto" to null,
                        "perfilCompleto" to false
                    )

                    // Crear documento users/{uid}
                    firestore.collection("users")
                        .document(user.uid)
                        .set(userData)
                        .addOnSuccessListener {

                            // Enviar correo de verificación
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

                    val user = firebaseAuth.currentUser

                    user?.reload()?.addOnCompleteListener { reloadTask ->

                        if (!reloadTask.isSuccessful) {
                            firebaseAuth.signOut()
                            onResult(
                                false,
                                null,
                                "No se ha podido comprobar tu cuenta."
                            )
                            return@addOnCompleteListener
                        }

                        val updatedUser = firebaseAuth.currentUser

                        if (updatedUser?.isEmailVerified != true) {
                            firebaseAuth.signOut()
                            onResult(
                                false,
                                null,
                                "Debes verificar tu correo electrónico antes de iniciar sesión."
                            )
                            return@addOnCompleteListener
                        }

                        // Consultar el perfil en Firestore
                        firestore.collection("users")
                            .document(updatedUser.uid)
                            .get()
                            .addOnSuccessListener { document ->

                                if (document.exists()) {

                                    val perfilCompleto =
                                        document.getBoolean("perfilCompleto")
                                            ?: false

                                    onResult(
                                        true,
                                        perfilCompleto,
                                        null
                                    )

                                } else {
                                    // Si no existe el documento,
                                    // consideramos que falta configurar el perfil.
                                    onResult(
                                        true,
                                        false,
                                        null
                                    )
                                }
                            }
                            .addOnFailureListener { exception ->
                                firebaseAuth.signOut()
                                onResult(
                                    false,
                                    null,
                                    "Error al consultar tu perfil: ${exception.message}"
                                )
                            }

                    } ?: run {
                        firebaseAuth.signOut()
                        onResult(
                            false,
                            null,
                            "No se ha podido obtener el usuario."
                        )
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
