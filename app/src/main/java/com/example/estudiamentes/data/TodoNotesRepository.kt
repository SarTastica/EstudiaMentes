package com.example.estudiamentes.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

data class TodoItem(
    val id: String = "",
    val title: String = "",
    val completed: Boolean = false
)

data class NoteItem(
    val id: String = "",
    val title: String = "",
    val content: String = ""
)

class TodoNotesRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun userCollection(collection: String) =
        auth.currentUser?.uid?.let { uid ->
            db.collection("users")
                .document(uid)
                .collection(collection)
        }

    // ---------------- TO-DO ----------------

    fun listenTodos(
        onResult: (List<TodoItem>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration? {
        return userCollection("todos")?.addSnapshotListener { snapshot, error ->
            if (error != null) {
                onError(error)
                return@addSnapshotListener
            }

            val todos = snapshot?.documents?.mapNotNull { document ->
                TodoItem(
                    id = document.id,
                    title = document.getString("title") ?: "",
                    completed = document.getBoolean("completed") ?: false
                )
            } ?: emptyList()

            onResult(todos)
        }
    }

    fun addTodo(title: String, onComplete: (Exception?) -> Unit) {
        val data = hashMapOf(
            "title" to title,
            "completed" to false,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        val collection = userCollection("todos")
        if (collection == null) {
            onComplete(Exception("No hay un usuario autenticado"))
            return
        }

        collection.add(data)
            .addOnSuccessListener { onComplete(null) }
            .addOnFailureListener { onComplete(it) }
    }

    fun updateTodo(
        todoId: String,
        title: String,
        onComplete: (Exception?) -> Unit
    ) {
        val collection = userCollection("todos")
        if (collection == null) {
            onComplete(Exception("No hay un usuario autenticado"))
            return
        }

        collection.document(todoId)
            .update(
                mapOf(
                    "title" to title,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            .addOnSuccessListener { onComplete(null) }
            .addOnFailureListener { onComplete(it) }
    }

    fun toggleTodo(
        todoId: String,
        completed: Boolean,
        onComplete: (Exception?) -> Unit
    ) {
        val collection = userCollection("todos")
        if (collection == null) {
            onComplete(Exception("No hay un usuario autenticado"))
            return
        }

        collection.document(todoId)
            .update(
                mapOf(
                    "completed" to completed,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            .addOnSuccessListener { onComplete(null) }
            .addOnFailureListener { onComplete(it) }
    }

    fun deleteTodo(todoId: String, onComplete: (Exception?) -> Unit) {
        val collection = userCollection("todos")
        if (collection == null) {
            onComplete(Exception("No hay un usuario autenticado"))
            return
        }

        collection.document(todoId)
            .delete()
            .addOnSuccessListener { onComplete(null) }
            .addOnFailureListener { onComplete(it) }
    }

    // ---------------- NOTAS ----------------

    fun listenNotes(
        onResult: (List<NoteItem>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration? {
        return userCollection("notes")?.addSnapshotListener { snapshot, error ->
            if (error != null) {
                onError(error)
                return@addSnapshotListener
            }

            val notes = snapshot?.documents?.map { document ->
                NoteItem(
                    id = document.id,
                    title = document.getString("title") ?: "",
                    content = document.getString("content") ?: ""
                )
            } ?: emptyList()

            onResult(notes)
        }
    }

    fun addNote(
        title: String,
        content: String,
        onComplete: (Exception?) -> Unit
    ) {
        val data = hashMapOf(
            "title" to title,
            "content" to content,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        val collection = userCollection("notes")
        if (collection == null) {
            onComplete(Exception("No hay un usuario autenticado"))
            return
        }

        collection.add(data)
            .addOnSuccessListener { onComplete(null) }
            .addOnFailureListener { onComplete(it) }
    }

    fun updateNote(
        noteId: String,
        title: String,
        content: String,
        onComplete: (Exception?) -> Unit
    ) {
        val collection = userCollection("notes")
        if (collection == null) {
            onComplete(Exception("No hay un usuario autenticado"))
            return
        }

        collection.document(noteId)
            .update(
                mapOf(
                    "title" to title,
                    "content" to content,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            .addOnSuccessListener { onComplete(null) }
            .addOnFailureListener { onComplete(it) }
    }

    fun deleteNote(noteId: String, onComplete: (Exception?) -> Unit) {
        val collection = userCollection("notes")
        if (collection == null) {
            onComplete(Exception("No hay un usuario autenticado"))
            return
        }

        collection.document(noteId)
            .delete()
            .addOnSuccessListener { onComplete(null) }
            .addOnFailureListener { onComplete(it) }
    }
}
