package com.iqra.chinese.firebase

import android.content.Context
import androidx.core.content.edit
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import org.json.JSONObject

/**
 * Remote banner / announcement system.
 *
 * Push a banner by writing to Realtime Database at:
 *   /banners/active
 * with fields:
 *   id          (string) — unique id, e.g. "promo_2026_06" — used to track dismissal
 *   title       (string) — bold headline
 *   message     (string) — body text
 *   imageUrl    (string, optional) — image shown above the text
 *   actionLabel (string, optional) — button text, e.g. "Learn more"
 *   actionUrl   (string, optional) — URL opened when button tapped
 *   enabled     (bool)   — set false to hide the banner for everyone
 *
 * Example JSON to push via Firebase Console → Realtime Database:
 * {
 *   "banners": {
 *     "active": {
 *       "id": "summer_promo",
 *       "title": "🎉 New HSK 6 content!",
 *       "message": "We've added 500 new vocabulary words to HSK 6. Check them out!",
 *       "imageUrl": "https://yourcdn.com/banner.png",
 *       "actionLabel": "Open",
 *       "actionUrl": "https://yourapp.com/whatsnew",
 *       "enabled": true
 *     }
 *   }
 * }
 *
 * Behavior:
 *  - Fetched once per app session (on MainActivity launch) when internet is available
 *  - Cached locally in SharedPrefs so it can be shown even if offline on next launch
 *  - Once user dismisses a banner (by id), it won't show again — until a NEW id is pushed
 */
data class BannerConfig(
    val id: String,
    val title: String,
    val message: String,
    val imageUrl: String? = null,
    val videoUrl: String? = null,        // direct video URL or YouTube URL
    val actionLabel: String? = null,
    val actionUrl: String? = null,
    val feedback: Boolean = false,       // if true, show feedback input field
    val feedbackQuestion: String? = null, // question shown above the feedback field
    val enabled: Boolean = true
)

object BannerManager {

    private const val PREFS = "iqra_banner"
    private const val KEY_CACHED   = "cached_json"
    private const val KEY_DISMISSED = "dismissed_id"

    private val db by lazy {
        Firebase.database("https://iqra-chinese-default-rtdb.firebaseio.com")
            .reference.child("banners").child("active")
    }

    /**
     * Fetches the latest banner from Firebase (if online) and caches it locally.
     * Returns the banner to show, or null if none/dismissed/disabled.
     * Safe to call on every app launch — does nothing harmful if offline.
     */
    suspend fun fetchBanner(ctx: Context): BannerConfig? {
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        // Try to fetch fresh data from Firebase (with short timeout via coroutine)
        val remote = runCatching {
            val snapshot = db.get().await()
            if (!snapshot.exists()) return@runCatching null
            snapshotToJson(snapshot)
        }.getOrNull()

        // Cache whatever we got (even if null result means "no banner configured")
        if (remote != null) {
            prefs.edit { putString(KEY_CACHED, remote.toString()) }
        }

        // Use fresh data if available, otherwise fall back to cache (offline support)
        val json = remote ?: prefs.getString(KEY_CACHED, null)?.let {
            runCatching { JSONObject(it) }.getOrNull()
        } ?: return null

        return parseConfig(json, prefs)
    }

    /**
     * Attaches a live listener to /banners/active so that if an admin pushes a
     * NEW or UPDATED banner while the app is open, it is delivered immediately —
     * no app restart needed.
     *
     * [onBanner] is called with the new banner config whenever it changes and
     * passes the dismissed/disabled checks, or null if it should be hidden.
     * Caller is responsible for removing the listener (see [removeLiveListener])
     * when the host is destroyed.
     *
     * Returns the listener handle so it can be detached later.
     */
    fun startLiveListener(ctx: Context, onBanner: (BannerConfig?) -> Unit): ValueEventListener {
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) { onBanner(null); return }
                val json = snapshotToJson(snapshot)
                prefs.edit { putString(KEY_CACHED, json.toString()) }
                onBanner(parseConfig(json, prefs))
            }
            override fun onCancelled(error: DatabaseError) { /* offline-safe: ignore */ }
        }
        db.addValueEventListener(listener)
        return listener
    }

    /** Detach a listener started with [startLiveListener]. Call in onDestroy(). */
    fun removeLiveListener(listener: ValueEventListener) {
        db.removeEventListener(listener)
    }

    private fun snapshotToJson(snapshot: DataSnapshot): JSONObject = JSONObject().apply {
        snapshot.children.forEach { child ->
            put(child.key ?: return@forEach, child.value)
        }
    }

    private fun parseConfig(json: JSONObject, prefs: android.content.SharedPreferences): BannerConfig? {
        val config = runCatching {
            BannerConfig(
                id               = json.optString("id", ""),
                title            = json.optString("title", ""),
                message          = json.optString("message", ""),
                imageUrl         = json.optString("imageUrl", "").ifEmpty { null },
                videoUrl         = json.optString("videoUrl", "").ifEmpty { null },
                actionLabel      = json.optString("actionLabel", "").ifEmpty { null },
                actionUrl        = json.optString("actionUrl", "").ifEmpty { null },
                feedback         = json.optBoolean("feedback", false),
                feedbackQuestion = json.optString("feedbackQuestion", "").ifEmpty { null },
                enabled          = json.optBoolean("enabled", true)
            )
        }.getOrNull() ?: return null

        if (!config.enabled || config.id.isEmpty()) return null

        // Don't show if user already dismissed this exact banner id
        val dismissedId = prefs.getString(KEY_DISMISSED, null)
        if (dismissedId == config.id) return null

        return config
    }

    /** Call when user taps the X / close on the banner */
    fun dismiss(ctx: Context, bannerId: String) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit { putString(KEY_DISMISSED, bannerId) }
        incrementViewCount(bannerId)
    }

    /**
     * Saves a user's feedback response to:
     *   /banners/feedback/{bannerId}/{uid_or_anon}/{timestamp}
     *     response: "user's answer text"
     *     uid: "user uid or anonymous"
     *     ts: epoch ms
     */
    fun submitFeedback(bannerId: String, response: String, uid: String?) {
        val safeId   = bannerId.replace(".", "_").replace("#", "_")
            .replace("$", "_").replace("[", "_").replace("]", "_").replace("/", "_")
        val userKey  = uid?.ifEmpty { "anon" } ?: "anon"
        val ref      = Firebase.database("https://iqra-chinese-default-rtdb.firebaseio.com")
            .reference
            .child("banners").child("feedback").child(safeId).child(userKey)

        ref.setValue(mapOf(
            "response" to response,
            "uid"      to userKey,
            "ts"       to System.currentTimeMillis()
        ))
    }

    /**
     * Increments a global view/dismiss counter for this banner id in Firebase,
     * stored at /banners/stats/{bannerId}/dismissCount.
     * Fire-and-forget — failures are silently ignored (offline-safe).
     */
    private fun incrementViewCount(bannerId: String) {
        val safeId = bannerId.replace(".", "_").replace("#", "_")
            .replace("$", "_").replace("[", "_").replace("]", "_").replace("/", "_")
        val ref = Firebase.database("https://iqra-chinese-default-rtdb.firebaseio.com")
            .reference.child("banners").child("stats").child(safeId).child("dismissCount")

        ref.get().addOnSuccessListener { snapshot ->
            val current = (snapshot.value as? Long) ?: 0L
            ref.setValue(current + 1)
        }
    }
}
