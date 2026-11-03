package com.example.estudiamentes.data
import com.google.firebase.auth.FirebaseAuth

class AuthRepository {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    fun register(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    val user = firebaseAuth.currentUser

                    user?.sendEmailVerification()
                        ?.addOnCompleteListener { verificationTask ->

                            if (verificationTask.isSuccessful) {
                                firebaseAuth.signOut()
                                onResult(true, null)
                            } else {
                                firebaseAuth.signOut()
                                onResult(
                                    false,
                                    "No se ha podido enviar el correo de verificación"
                                )
                            }
                        }

                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun login(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    val user = firebaseAuth.currentUser

                    user?.reload()?.addOnCompleteListener {

                        val updatedUser = firebaseAuth.currentUser

                        if (updatedUser?.isEmailVerified == true) {
                            onResult(true, null)
                        } else {
                            firebaseAuth.signOut()
                            onResult(
                                false,
                                "Debes verificar tu correo electrónico antes de iniciar sesión."
                            )
                        }
                    }

                } else {
                    onResult(
                        false,
                        task.exception?.message ?: "No se ha podido iniciar sesión"
                    )
                }
            }
    }
}
