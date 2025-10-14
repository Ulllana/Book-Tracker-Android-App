package com.example.bookcatalogapp.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookcatalogapp.api.RetrofitInstance
import com.example.bookcatalogapp.models.VolumeItem
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val _searchResults = MutableLiveData<List<VolumeItem>>()
    val searchResults: LiveData<List<VolumeItem>> get() = _searchResults

    fun searchBooks(query: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.searchBooks(query)
                _searchResults.value = response.items ?: emptyList()
            } catch (e: Exception) {
                // Handle error, e.g., post empty list or error state
            }
        }
    }
}