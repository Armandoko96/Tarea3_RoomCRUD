package com.curso.tarea3.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.curso.tarea3.data.Book
import com.curso.tarea3.data.BooksRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BooksViewModel(private val repository: BooksRepository) : ViewModel() {
    val booksState: StateFlow<List<Book>> = repository.getAllBooksStream()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun insertBook(book: Book) {
        viewModelScope.launch { repository.insertBook(book) }
    }

    fun updateBook(book: Book) {
        viewModelScope.launch { repository.updateBook(book) }
    }

    fun deleteBook(book: Book) {
        viewModelScope.launch { repository.deleteBook(book) }
    }
}
