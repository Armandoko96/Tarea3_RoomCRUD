package com.curso.tarea3

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.curso.tarea3.data.Book
import com.curso.tarea3.ui.AppViewModelProvider
import com.curso.tarea3.ui.BooksViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation()
                }
            }
        }
    }
}

enum class AppScreen { LOGIN, LIST, ADD, EDIT }

@Composable
fun AppNavigation(viewModel: BooksViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)
    val usuarioGuardado = prefs.getString("user", null)

    var pantalla by remember { mutableStateOf(if (usuarioGuardado != null) AppScreen.LIST else AppScreen.LOGIN) }
    var libroSeleccionado by remember { mutableStateOf<Book?>(null) }

    when (pantalla) {
        AppScreen.LOGIN -> LoginScreen(
            onLoginSuccess = { user ->
                prefs.edit().putString("user", user).apply()
                pantalla = AppScreen.LIST
            }
        )
        AppScreen.LIST -> BookListScreen(
            viewModel = viewModel,
            onNavigateToAdd = { pantalla = AppScreen.ADD },
            onNavigateToEdit = { book ->
                libroSeleccionado = book
                pantalla = AppScreen.EDIT
            },
            onLogout = {
                prefs.edit().remove("user").apply()
                pantalla = AppScreen.LOGIN
            }
        )
        AppScreen.ADD -> BookAddScreen(
            onSave = { book ->
                viewModel.insertBook(book)
                pantalla = AppScreen.LIST
            },
            onCancel = { pantalla = AppScreen.LIST }
        )
        AppScreen.EDIT -> libroSeleccionado?.let {
            BookEditScreen(
                book = it,
                onSave = { updated ->
                    viewModel.updateBook(updated)
                    pantalla = AppScreen.LIST
                },
                onCancel = { pantalla = AppScreen.LIST }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit) {
    var usuario by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var errorVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Iniciar Sesión") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text("Catálogo de Libros", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(30.dp))

            OutlinedTextField(
                value = usuario,
                onValueChange = { usuario = it; errorVisible = false },
                label = { Text("Usuario") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = contrasena,
                onValueChange = { contrasena = it; errorVisible = false },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (errorVisible) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Usuario o contraseña incorrectos", color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    if (usuario == "admin" && contrasena == "1234") {
                        onLoginSuccess(usuario)
                    } else {
                        errorVisible = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ingresar")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookListScreen(
    viewModel: BooksViewModel,
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (Book) -> Unit,
    onLogout: () -> Unit
) {
    val libros by viewModel.booksState.collectAsState()
    val cargando by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Listado de Libros") },
                actions = {
                    // boton para sincronizar con la api
                    IconButton(onClick = { viewModel.syncFromApi() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sincronizar")
                    }
                    TextButton(onClick = onLogout) { Text("Salir") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (cargando) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (libros.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No hay libros guardados.")
                            }
                        }
                    }
                    items(libros) { libro ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(libro.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("Autor: ${libro.author}", style = MaterialTheme.typography.bodyMedium)
                                    Text("Género: ${libro.genre}", style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        text = String.format("$%.2f | %d págs", libro.price, libro.pages),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Row {
                                    IconButton(onClick = { onNavigateToEdit(libro) }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                                    }
                                    IconButton(onClick = { viewModel.deleteBook(libro) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = Color.Red)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookAddScreen(onSave: (Book) -> Unit, onCancel: () -> Unit) {
    var titulo by remember { mutableStateOf("") }
    var autor by remember { mutableStateOf("") }
    var genero by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var paginas by remember { mutableStateOf("") }

    Scaffold(topBar = { TopAppBar(title = { Text("Agregar Libro") }) }) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(value = titulo, onValueChange = { titulo = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = autor, onValueChange = { autor = it }, label = { Text("Autor") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = genero, onValueChange = { genero = it }, label = { Text("Género") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = precio, onValueChange = { precio = it }, label = { Text("Precio ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = paginas, onValueChange = { paginas = it }, label = { Text("Páginas") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onCancel) { Text("Cancelar") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    val p = precio.toDoubleOrNull() ?: 0.0
                    val pags = paginas.toIntOrNull() ?: 0
                    if (titulo.isNotBlank() && autor.isNotBlank() && genero.isNotBlank() && p > 0 && pags > 0) {
                        onSave(Book(title = titulo, author = autor, genre = genero, price = p, pages = pags))
                    }
                }) { Text("Guardar") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookEditScreen(book: Book, onSave: (Book) -> Unit, onCancel: () -> Unit) {
    var titulo by remember { mutableStateOf(book.title) }
    var autor by remember { mutableStateOf(book.author) }
    var genero by remember { mutableStateOf(book.genre) }
    var precio by remember { mutableStateOf(book.price.toString()) }
    var paginas by remember { mutableStateOf(book.pages.toString()) }

    Scaffold(topBar = { TopAppBar(title = { Text("Editar Libro") }) }) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(value = titulo, onValueChange = { titulo = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = autor, onValueChange = { autor = it }, label = { Text("Autor") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = genero, onValueChange = { genero = it }, label = { Text("Género") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = precio, onValueChange = { precio = it }, label = { Text("Precio ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = paginas, onValueChange = { paginas = it }, label = { Text("Páginas") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onCancel) { Text("Cancelar") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    val p = precio.toDoubleOrNull() ?: 0.0
                    val pags = paginas.toIntOrNull() ?: 0
                    if (titulo.isNotBlank() && autor.isNotBlank() && genero.isNotBlank() && p > 0 && pags > 0) {
                        onSave(book.copy(title = titulo, author = autor, genre = genero, price = p, pages = pags))
                    }
                }) { Text("Actualizar") }
            }
        }
    }
}
