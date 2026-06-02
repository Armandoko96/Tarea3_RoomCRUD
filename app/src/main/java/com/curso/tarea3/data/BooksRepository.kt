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
        try {
            // primero creamos en la API para que ella asigne el ID correcto
            RetrofitInstance.api.create(book.toBookApi())
            // luego sincronizamos Room con lo que tiene la API (IDs correctos)
            syncFromApi()
        } catch (e: Exception) {
            // sin internet: guardamos local como respaldo
            bookDao.insert(book)
            e.printStackTrace()
        }
    }

    override suspend fun deleteBook(book: Book) {
        bookDao.delete(book)
        try { RetrofitInstance.api.delete(book.id) } catch (e: Exception) { e.printStackTrace() }
    }

    override suspend fun updateBook(book: Book) {
        bookDao.update(book)
        try { RetrofitInstance.api.update(book.toBookApi()) } catch (e: Exception) { e.printStackTrace() }
    }

    override suspend fun syncFromApi() {
        try {
            val librosApi = RetrofitInstance.api.getAll()
            // borramos y re-insertamos para que los IDs de la API queden correctos
            bookDao.deleteAll()
            for (libro in librosApi) {
                bookDao.insert(libro.toBook())
            }
        } catch (e: Exception) {
            // si no hay conexion simplemente usamos los datos locales
            e.printStackTrace()
        }
    }
}
