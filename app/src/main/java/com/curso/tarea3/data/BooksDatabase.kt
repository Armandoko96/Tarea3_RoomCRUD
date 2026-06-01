package com.curso.tarea3.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Book::class], version = 1, exportSchema = false)
abstract class BooksDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao

    companion object {
        @Volatile
        private var Instance: BooksDatabase? = null

        fun getDatabase(context: Context): BooksDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context.applicationContext, BooksDatabase::class.java, "books_db")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
