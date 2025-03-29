package com.daniela.pillbox.data.services

import android.util.Log
import io.appwrite.Client
import io.appwrite.ID
import io.appwrite.exceptions.AppwriteException
import io.appwrite.models.User
import io.appwrite.services.Account

class AccountService(client: Client) {
    private val account = Account(client)

    suspend fun getLoggedIn(): User<Map<String, Any>>? {
        return try {
            account.get()
        } catch (e: AppwriteException) {
            Log.e("AccountService", "Error getting logged in user", e)
            null
        }
    }

    suspend fun login(email: String, password: String): Boolean {
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

    suspend fun register(email: String, password: String, name: String): Boolean {
        return try {
            // Create the user
            val user = account.create(ID.unique(), email, password, name)
            Log.d("AccountService", "User created: $user")
            true
        } catch (e: AppwriteException) {
            Log.e("AccountService", "Registration error", e)
            false
        }
    }

    suspend fun logout() {
        try {
            account.deleteSession("current")
            Log.d("AccountService", "Logged out successfully")
        } catch (e: AppwriteException) {
            Log.e("AccountService", "Logout error", e)
        }
    }
}