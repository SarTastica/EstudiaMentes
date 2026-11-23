package com.example.estudiamentes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.estudiamentes.data.NoteItem
import com.example.estudiamentes.data.TodoItem
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.DisposableEffect
import com.example.estudiamentes.data.TodoNotesRepository

private val TodoGreen = Color(0xFF70D4B8)
private val TodoGreenBg = Color(0xFFD9F4EA)
private val NotePurple = Color(0xFFA77ACB)
private val NotePurpleBg = Color(0xFFF2E9FF)
private val CrudDark = Color(0xFF1A1F36)

@Composable
fun TodoNotesSection() {
    val repository = remember { TodoNotesRepository() }

    val todos = remember { mutableStateListOf<TodoItem>() }
    val notes = remember { mutableStateListOf<NoteItem>() }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showTodoDialog by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }

    var editingTodo by remember { mutableStateOf<TodoItem?>(null) }
    var editingNote by remember { mutableStateOf<NoteItem?>(null) }

    DisposableEffect(Unit) {
        val todoListener = repository.listenTodos(
            onResult = { result ->
                todos.clear()
                todos.addAll(result)
            },
            onError = { error ->
                errorMessage = error.message ?: "Error al cargar las tareas"
            }
        )

        val noteListener = repository.listenNotes(
            onResult = { result ->
                notes.clear()
                notes.addAll(result)
            },
            onError = { error ->
                errorMessage = error.message ?: "Error al cargar las notas"
            }
        )

        onDispose {
            todoListener?.remove()
            noteListener?.remove()
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ---------------- TO-DO ----------------
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = TodoGreenBg
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✓",
                            color = Color.White,
                            fontSize = 17.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(7.dp))
                                .background(TodoGreen)
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        )

                        Spacer(modifier = Modifier.width(7.dp))

                        Text(
                            text = "To-Do",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CrudDark
                        )
                    }

                    if (todos.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "${todos.count { !it.completed }} pendientes",
                            fontSize = 11.sp,
                            color = Color(0xFF368F7C)
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                    }


                    if (todos.isEmpty()) {
                        Text(
                            text = "Todavía no tienes tareas.",
                            fontSize = 11.sp,
                            color = Color(0xFF668A91)
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 190.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            todos.forEach { todo ->
                                var menuExpanded by remember(todo.id) {
                                    mutableStateOf(false)
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = todo.completed,
                                        onCheckedChange = { checked ->
                                            repository.toggleTodo(todo.id, checked) { error ->
                                                if (error != null) {
                                                    errorMessage = error.message
                                                }
                                            }
                                        },
                                        modifier = Modifier.size(32.dp),
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = TodoGreen
                                        )
                                    )

                                    Text(
                                        text = todo.title,
                                        fontSize = 11.sp,
                                        color = CrudDark,
                                        textDecoration = if (todo.completed) {
                                            TextDecoration.LineThrough
                                        } else {
                                            TextDecoration.None
                                        },
                                        modifier = Modifier.weight(1f)
                                    )

                                    Box {
                                        IconButton(
                                            onClick = { menuExpanded = true },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MoreVert,
                                                contentDescription = "Opciones de tarea",
                                                tint = CrudDark,
                                                modifier = Modifier.size(19.dp)
                                            )
                                        }

                                        DropdownMenu(
                                            expanded = menuExpanded,
                                            onDismissRequest = {
                                                menuExpanded = false
                                            }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Editar") },
                                                leadingIcon = {
                                                    Icon(
                                                        Icons.Default.Edit,
                                                        contentDescription = null
                                                    )
                                                },
                                                onClick = {
                                                    menuExpanded = false
                                                    editingTodo = todo
                                                    showTodoDialog = true
                                                }
                                            )

                                            DropdownMenuItem(
                                                text = { Text("Borrar") },
                                                leadingIcon = {
                                                    Icon(
                                                        Icons.Default.Delete,
                                                        contentDescription = null
                                                    )
                                                },
                                                onClick = {
                                                    menuExpanded = false
                                                    repository.deleteTodo(todo.id) { error ->
                                                        if (error != null) {
                                                            errorMessage = error.message
                                                        }
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }


                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            editingTodo = null
                            showTodoDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFB8ECDD),
                            contentColor = Color(0xFF287A68)
                        )
                    ) {
                        Text("+ Añadir", fontSize = 11.sp)
                    }
                }
            }

            // ---------------- NOTAS ----------------
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = NotePurpleBg
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "▤",
                            color = Color.White,
                            fontSize = 17.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(7.dp))
                                .background(NotePurple)
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        )

                        Spacer(modifier = Modifier.width(7.dp))

                        Text(
                            text = "Notas",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CrudDark
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))


                    if (notes.isEmpty()) {
                        Text(
                            text = "Todavía no tienes notas.",
                            fontSize = 11.sp,
                            color = Color(0xFF8961B5)
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 190.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            notes.forEach { note ->
                                var menuExpanded by remember(note.id) {
                                    mutableStateOf(false)
                                }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFFAF7FF)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 9.dp, end = 3.dp, top = 7.dp, bottom = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = note.title,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = CrudDark
                                            )

                                            Spacer(modifier = Modifier.height(3.dp))

                                            Text(
                                                text = note.content,
                                                fontSize = 10.sp,
                                                color = Color(0xFF7886A5),
                                                maxLines = 2
                                            )
                                        }

                                        Box {
                                            IconButton(
                                                onClick = { menuExpanded = true },
                                                modifier = Modifier.size(30.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.MoreVert,
                                                    contentDescription = "Opciones de nota",
                                                    tint = CrudDark,
                                                    modifier = Modifier.size(19.dp)
                                                )
                                            }

                                            DropdownMenu(
                                                expanded = menuExpanded,
                                                onDismissRequest = {
                                                    menuExpanded = false
                                                }
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("Editar") },
                                                    leadingIcon = {
                                                        Icon(
                                                            Icons.Default.Edit,
                                                            contentDescription = null
                                                        )
                                                    },
                                                    onClick = {
                                                        menuExpanded = false
                                                        editingNote = note
                                                        showNoteDialog = true
                                                    }
                                                )

                                                DropdownMenuItem(
                                                    text = { Text("Borrar") },
                                                    leadingIcon = {
                                                        Icon(
                                                            Icons.Default.Delete,
                                                            contentDescription = null
                                                        )
                                                    },
                                                    onClick = {
                                                        menuExpanded = false
                                                        repository.deleteNote(note.id) { error ->
                                                            if (error != null) {
                                                                errorMessage = error.message
                                                            }
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }


                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            editingNote = null
                            showNoteDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE5D2FA),
                            contentColor = Color(0xFF7950A4)
                        )
                    ) {
                        Text("+ Añadir", fontSize = 11.sp)
                    }
                }
            }
        }

        errorMessage?.let { message ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp
            )
        }
    }

    // DIÁLOGO PARA CREAR O EDITAR TAREAS
    if (showTodoDialog) {
        TodoEditorDialog(
            initialTitle = editingTodo?.title ?: "",
            onDismiss = { showTodoDialog = false },
            onSave = { title ->
                if (editingTodo == null) {
                    repository.addTodo(title) { error ->
                        if (error != null) errorMessage = error.message
                    }
                } else {
                    repository.updateTodo(
                        editingTodo!!.id,
                        title
                    ) { error ->
                        if (error != null) errorMessage = error.message
                    }
                }
                showTodoDialog = false
            }
        )
    }

    // DIÁLOGO PARA CREAR O EDITAR NOTAS
    if (showNoteDialog) {
        NoteEditorDialog(
            initialTitle = editingNote?.title ?: "",
            initialContent = editingNote?.content ?: "",
            onDismiss = { showNoteDialog = false },
            onSave = { title, content ->
                if (editingNote == null) {
                    repository.addNote(title, content) { error ->
                        if (error != null) errorMessage = error.message
                    }
                } else {
                    repository.updateNote(
                        editingNote!!.id,
                        title,
                        content
                    ) { error ->
                        if (error != null) errorMessage = error.message
                    }
                }
                showNoteDialog = false
            }
        )
    }
}

@Composable
private fun TodoEditorDialog(
    initialTitle: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var title by remember(initialTitle) {
        mutableStateOf(initialTitle)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initialTitle.isEmpty()) "Nueva tarea" else "Editar tarea")
        },
        text = {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Nombre de la tarea") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(title.trim()) },
                enabled = title.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun NoteEditorDialog(
    initialTitle: String,
    initialContent: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var title by remember(initialTitle) {
        mutableStateOf(initialTitle)
    }

    var content by remember(initialContent) {
        mutableStateOf(initialContent)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initialTitle.isEmpty()) "Nueva nota" else "Editar nota")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Escribe tu nota") },
                    minLines = 3,
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(title.trim(), content.trim()) },
                enabled = title.isNotBlank() && content.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
