package com.curso.tarea3.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.curso.tarea3.BooksApplication

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            BooksViewModel(booksApplication().container.booksRepository)
        }
    }
}

fun CreationExtras.booksApplication(): BooksApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as BooksApplication)
