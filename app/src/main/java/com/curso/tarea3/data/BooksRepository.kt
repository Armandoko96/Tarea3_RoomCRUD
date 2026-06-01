package com.curso.tarea3.data

import kotlinx.coroutines.flow.Flow

interface BooksRepository {
    fun getAllBooksStream(): Flow<List<Book>>
    suspend fun insertBook(book: Book)
    suspend fun deleteBook(book: Book)
    suspend fun updateBook(book: Book)
}

class OfflineBooksRepository(private val bookDao: BookDao) : BooksRepository {
    override fun getAllBooksStream(): Flow<List<Book>> = bookDao.getAllBooks()
    override suspend fun insertBook(book: Book) = bookDao.insert(book)
    override suspend fun deleteBook(book: Book) = bookDao.delete(book)
    override suspend fun updateBook(book: Book) = bookDao.update(book)
}
