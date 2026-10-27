package com.iqra.chinese

import android.app.Application
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class IqraApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Keep not-yet-acknowledged writes on disk. A progress push made just before
        // the app is killed (swiped away, or reclaimed by the system) is then still
        // delivered to the server the next time the app starts, instead of being lost.
        // Must run before anything else touches this database instance.
        try {
            Firebase.database("https://iqra-chinese-default-rtdb.firebaseio.com")
                .setPersistenceEnabled(true)
        } catch (_: Exception) {
            // Already initialised — harmless, writes are simply kept in memory only
        }
    }
}
