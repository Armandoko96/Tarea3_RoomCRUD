package com.curso.tarea3.data

import kotlinx.coroutines.flow.Flow

interface BooksRepository {
    fun getAllBooksStream(): Flow<List<Book>>
    suspend fun insertBook(book: Book)
    suspend fun deleteBook(book: Book)
    suspend fun updateBook(book: Book)
    suspend fun syncFromApi()
}

class OfflineBooksRepository(private val bookDao: BookDao) : BooksRepository {

    override fun getAllBooksStream(): Flow<List<Book>> = bookDao.getAllBooks()

    override suspend fun insertBook(book: Book) {
        bookDao.insert(book)
        // intentamos tambien guardarlo en la api, si falla no pasa nada
        try {
            RetrofitInstance.api.create(book.toBookApi())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun deleteBook(book: Book) {
        bookDao.delete(book)
        try {
            RetrofitInstance.api.delete(book.id)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun updateBook(book: Book) {
        bookDao.update(book)
        try {
            RetrofitInstance.api.update(book.toBookApi())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun syncFromApi() {
        try {
            val librosApi = RetrofitInstance.api.getAll()
            for (libro in librosApi) {
                bookDao.upsert(libro.toBook())
            }
        } catch (e: Exception) {
            // si no hay conexion simplemente usamos los datos locales
            e.printStackTrace()
        }
    }
}
