package com.daniela.pillbox

import android.content.Context
import com.daniela.pillbox.data.services.AccountService
import io.appwrite.Client

object Appwrite {
    lateinit var account: AccountService
    private var client: Client? = null

    /*fun init(ctx: Context) {
        client = Client(ctx).setProject("67e6c4c3002ebd01cdaa")

        account = AccountService(client)
    }*/

    fun getClient(ctx: Context): Client {
        synchronized(this) {
            if (client == null) {
                client = Client(ctx).setProject("67e6c4c3002ebd01cdaa")
            }
            return client!!
        }
    }
}

// https://appwrite.io/docs/tutorials/android/step-4