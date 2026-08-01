package com.example.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.EnglishRepository

class EnglishViewModelFactory(
    private val application: Application,
    private val repository: EnglishRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EnglishViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EnglishViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
