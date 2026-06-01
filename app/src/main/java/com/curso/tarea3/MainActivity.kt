package com.curso.tarea3

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                    RoomCrudNavigation()
                }
            }
        }
    }
}

enum class CrudScreen {
    LIST, ADD, EDIT
}

@Composable
fun RoomCrudNavigation(viewModel: BooksViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    var screen by remember { mutableStateOf(CrudScreen.LIST) }
    var selectedBook by remember { mutableStateOf<Book?>(null) }

    when (screen) {
        CrudScreen.LIST -> BookListScreen(
            viewModel = viewModel,
            onNavigateToAdd = { screen = CrudScreen.ADD },
            onNavigateToEdit = { book ->
                selectedBook = book
                screen = CrudScreen.EDIT
            }
        )
        CrudScreen.ADD -> BookAddScreen(
            onSave = { book ->
                viewModel.insertBook(book)
                screen = CrudScreen.LIST
            },
            onCancel = { screen = CrudScreen.LIST }
        )
        CrudScreen.EDIT -> selectedBook?.let {
            BookEditScreen(
                book = it,
                onSave = { updated ->
                    viewModel.updateBook(updated)
                    screen = CrudScreen.LIST
                },
                onCancel = { screen = CrudScreen.LIST }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookListScreen(
    viewModel: BooksViewModel,
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (Book) -> Unit
) {
    val books by viewModel.booksState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Listado de Libros (Room)") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Libro")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (books.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay libros guardados en la BD.")
                    }
                }
            }
            items(books) { book ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = book.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Autor: ${book.author}", style = MaterialTheme.typography.bodyMedium)
                            Text("Género: ${book.genre}", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = String.format("$%.2f | %d págs", book.price, book.pages),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Row {
                            IconButton(onClick = { onNavigateToEdit(book) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar")
                            }
                            IconButton(onClick = { viewModel.deleteBook(book) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
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
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var pagesStr by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Agregar Libro") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = author, onValueChange = { author = it }, label = { Text("Autor") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = genre, onValueChange = { genre = it }, label = { Text("Género") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = priceStr,
                onValueChange = { priceStr = it },
                label = { Text("Precio ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = pagesStr,
                onValueChange = { pagesStr = it },
                label = { Text("Páginas") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onCancel) { Text("Cancelar") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    val price = priceStr.toDoubleOrNull() ?: 0.0
                    val pages = pagesStr.toIntOrNull() ?: 0
                    if (title.isNotBlank() && author.isNotBlank() && genre.isNotBlank() && price > 0 && pages > 0) {
                        onSave(Book(title = title, author = author, genre = genre, price = price, pages = pages))
                    }
                }) { Text("Guardar") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookEditScreen(book: Book, onSave: (Book) -> Unit, onCancel: () -> Unit) {
    var title by remember { mutableStateOf(book.title) }
    var author by remember { mutableStateOf(book.author) }
    var genre by remember { mutableStateOf(book.genre) }
    var priceStr by remember { mutableStateOf(book.price.toString()) }
    var pagesStr by remember { mutableStateOf(book.pages.toString()) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Editar Libro") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = author, onValueChange = { author = it }, label = { Text("Autor") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = genre, onValueChange = { genre = it }, label = { Text("Género") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = priceStr,
                onValueChange = { priceStr = it },
                label = { Text("Precio ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = pagesStr,
                onValueChange = { pagesStr = it },
                label = { Text("Páginas") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onCancel) { Text("Cancelar") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    val price = priceStr.toDoubleOrNull() ?: 0.0
                    val pages = pagesStr.toIntOrNull() ?: 0
                    if (title.isNotBlank() && author.isNotBlank() && genre.isNotBlank() && price > 0 && pages > 0) {
                        onSave(book.copy(title = title, author = author, genre = genre, price = price, pages = pages))
                    }
                }) { Text("Actualizar") }
            }
        }
    }
}
