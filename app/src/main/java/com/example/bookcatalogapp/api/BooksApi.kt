package com.example.bookcatalogapp.api

import com.example.bookcatalogapp.models.VolumeResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface BooksApi {
    @GET("volumes")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("key") apiKey: String = "AIzaSyAUiv14YYdUkWYssImVUr_QK-MusQ4aPEI",
        @Query("maxResults") maxResults: Int = 20
    ): VolumeResponse
}