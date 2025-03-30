package com.daniela.pillbox.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daniela.pillbox.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(val repository: AuthRepository) : ViewModel() {
    val user = repository.user
    val session = repository.session

    fun register(email: String, password: String, name: String) {
        viewModelScope.launch {
            repository.register(email, password, name)
        }
    }
}