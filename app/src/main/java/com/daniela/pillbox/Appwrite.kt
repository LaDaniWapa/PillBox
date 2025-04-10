package com.daniela.pillbox

import android.content.Context
import android.util.Log
import io.appwrite.Client
import io.appwrite.services.Databases

/**
 * Singleton class for Appwrite client and database services.
 */
object Appwrite {
    private var client: Client? = null
    private var dbs: Databases? = null

    /**
     * Returns the Appwrite client instance.
     */
    fun getClient(ctx: Context): Client {
        synchronized(this) {
            if (client == null) {
                client = Client(ctx)
                    .setEndpoint(BuildConfig.ENDPOINT)
                    .setProject(BuildConfig.PROJECT_ID)
                    .setSelfSigned(true)
            }
            return client!!
        }
    }

    /**
     * Returns the Appwrite database services instance.
     */
    fun getDatabases(ctx: Context): Databases {
        synchronized(this) {
            if (dbs == null) {
                dbs = Databases(getClient(ctx))
            }
            return dbs!!
        }
    }
}
