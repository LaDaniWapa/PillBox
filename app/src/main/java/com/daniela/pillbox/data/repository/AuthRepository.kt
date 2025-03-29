package com.daniela.pillbox.data.repository

import android.content.Context
import android.util.Log
import com.daniela.pillbox.Appwrite
import io.appwrite.Client
import io.appwrite.ID
import io.appwrite.exceptions.AppwriteException
import io.appwrite.models.User
import io.appwrite.services.Account
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepository(val ctx: Context) {
    private val _client = Appwrite.getClient(ctx)
    val client: Client get() = _client

    private val _user = MutableStateFlow<User<Map<String, Any>>?>(null)
    val user: StateFlow<User<Map<String, Any>>?> get() = _user.asStateFlow()

    suspend fun register(email: String, password: String, name: String): Boolean {
        val account = Account(client)

        return try {
            // Create the user
            val user = account.create(ID.unique(), email, password, name)
            _user.value = user
            Log.d("AccountService", "User created: $user")

            true
        } catch (e: AppwriteException) {
            _user.value = null
            Log.e("AccountService", "Registration error", e)

            false
        }
    }

    suspend fun login(email: String, password: String): Boolean {
        val account = Account(client)

        return try {
            // First create the session
            val session = account.createEmailPasswordSession(email, password)
            Log.d("AccountService", "Session created: $session")

            true
        } catch (e: AppwriteException) {
            Log.e("AccountService", "Login error", e)
            false
        }
    }
}