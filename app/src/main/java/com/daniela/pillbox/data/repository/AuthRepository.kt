package com.daniela.pillbox.data.repository

import android.content.Context
import android.util.Log
import com.daniela.pillbox.Appwrite
import io.appwrite.Client
import io.appwrite.ID
import io.appwrite.exceptions.AppwriteException
import io.appwrite.models.Session
import io.appwrite.models.User
import io.appwrite.services.Account
import kotlinx.coroutines.Delay
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant

class AuthRepository(val ctx: Context) {
    private val _client = Appwrite.getClient(ctx)
    val client: Client get() = _client

    private val _user = MutableStateFlow<User<Map<String, Any>>?>(null)
    val user: StateFlow<User<Map<String, Any>>?> get() = _user.asStateFlow()

    private val _session = MutableStateFlow<Session?>(null)
    val session: StateFlow<Session?> = _session.asStateFlow()

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
            _session.value = session
            Log.d("AccountService", "Session created: $session")

            true
        } catch (e: AppwriteException) {
            Log.e("AccountService", "Login error", e)
            _session.value = null
            false
        }
    }

    suspend fun getLoggedInUser(): User<Map<String, Any>>? {
        val account = Account(client)

        return try {
            refreshSession()

            val user = account.get()
            _user.value = user
            Log.d("AccountService", "User retrieved: $user")
            return user
        } catch (e: AppwriteException) {
            _user.value = null
            Log.e("AccountService", "Error retrieving user", e)
            null
        }
    }

    private fun isSessionExpired(session: Session): Boolean {
        return try {
            val expiryDate = Instant.parse(session.expire)
            Instant.now().isAfter(expiryDate)
        } catch (e: Exception) {
            true // If we can't parse the date, assume expired
        }
    }

    suspend fun refreshSession() {
        val account = Account(client)

        val session = account.getSession("current")
        if (isSessionExpired(session)) {
            account.updateSession(session.id)
            true
        }
    }

    suspend fun logout() {
        val account = Account(client)

        try {
            account.deleteSession("current")
            Log.d("AccountService", "Logged out successfully")
        } catch (e: AppwriteException) {
            Log.e("AccountService", "Logout error", e)
        }
    }
}