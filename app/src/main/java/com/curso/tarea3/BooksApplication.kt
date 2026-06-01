package com.curso.tarea3

import android.app.Application
import com.curso.tarea3.data.AppContainer
import com.curso.tarea3.data.AppDataContainer

class BooksApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}
