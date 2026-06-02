package com.curso.tarea3.data

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

data class BookApi(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("title") val title: String,
    @SerializedName("author") val author: String,
    @SerializedName("genre") val genre: String,
    @SerializedName("price") val price: Double,
    @SerializedName("pages") val pages: Int
)

fun BookApi.toBook() = Book(id = id, title = title, author = author, genre = genre, price = price, pages = pages)
fun Book.toBookApi() = BookApi(id = id, title = title, author = author, genre = genre, price = price, pages = pages)

interface BookApiService {
    @GET("libros.php")
    suspend fun getAll(): List<BookApi>

    @POST("libros.php")
    suspend fun create(@Body book: BookApi): Response<Unit>

    @PUT("libros.php")
    suspend fun update(@Body book: BookApi): Response<Unit>

    @DELETE("libros.php")
    suspend fun delete(@Query("id") id: Int): Response<Unit>
}

// desde el emulador, 10.0.2.2 es el localhost de la pc
object RetrofitInstance {
    private const val BASE_URL = "http://10.0.2.2/demoapi1/"

    val api: BookApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BookApiService::class.java)
    }
}
