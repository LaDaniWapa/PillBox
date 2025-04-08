package com.daniela.pillbox

import android.content.Context
import io.appwrite.Client

object Appwrite {
    private var client: Client? = null

    /*fun init(ctx: Context) {
        client = Client(ctx).setProject("67e6c4c3002ebd01cdaa")

        account = AccountService(client)
    }*/

    fun getClient(ctx: Context): Client {
        synchronized(this) {
            if (client == null) {
                client = Client(ctx)
                    .setEndpoint("https://appwrite.ladaniwapa.es/v1")
                    .setProject("67d311e500265a072556")
                    .setSelfSigned(true)
            }
            return client!!
        }
    }
}

// https://appwrite.io/docs/tutorials/android/step-4