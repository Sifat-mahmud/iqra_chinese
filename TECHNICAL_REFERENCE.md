# Iqra Chinese — Technical Reference Document

**Purpose of this document:** Paste this entire document into a new Claude conversation along with the project zip to give Claude full context on the app's architecture, all implemented features, and conventions used — so Claude can add features or fix bugs without rediscovering the codebase from scratch.

**How to use:** Upload the latest project zip + this document together. Tell Claude what you want changed or added.

---

## 1. App Identity

| Field | Value |
|---|---|
| App name | Iqra Chinese |
| Package / applicationId | `com.iqra.chinese` |
| namespace | `com.iqra.chinese` |
| compileSdk | 34 |
| minSdk | 26 |
| targetSdk | 34 |
| Language | Kotlin |
| UI | View-based (XML layouts + ViewBinding), single-Activity + Navigation Component fragments |
| Architecture | MVVM — `MainViewModel` (shared, scoped to Activity) + `Repository` + Room DB + SharedPreferences |
| Primary test device | Xiaomi/MIUI (HyperOS) — many fixes in this project specifically work around MIUI's background restrictions |
| Firebase project ID | `iqra-chinese` |
| Firebase RTDB URL | `https://iqra-chinese-default-rtdb.firebaseio.com` |

---

## 2. Project Structure

```
app/src/main/java/com/iqra/chinese/
├── MainActivity.kt              — single Activity host, bottom nav, banner overlay, alarm prompt, ExoPlayer
├── alarm/
│   ├── AlarmActivity.kt         — full-screen clock-style alarm UI (shows over lock screen)
│   ├── AlarmPrefs.kt            — day-start time config + per-phase snooze tracking
│   ├── AlarmReceiver.kt         — BroadcastReceiver, fires hourly, decides phase 0/1 behavior
│   ├── AlarmScheduler.kt        — uses AlarmManager.setAlarmClock() (clock-app-grade priority)
│   └── AlarmSoundService.kt     — foreground service, plays looping alarm sound + launches AlarmActivity
├── data/
│   ├── AppDatabase.kt           — Room database (AttemptRecord, StudyGroup)
│   ├── Dao.kt                   — AttemptDao, GroupDao
│   ├── HskData.kt               — static word/sentence/achievement data for HSK 1–6
│   ├── Models.kt                — Word, Sentence, AttemptRecord, StudyGroup, Achievement, etc.
│   ├── Prefs.kt                 — SharedPreferences wrapper ("iqra" prefs file) — XP, streak, goal, etc.
│   └── Repository.kt            — business logic layer between ViewModel and Room/Prefs/Firebase
├── firebase/
│   ├── BannerManager.kt         — remote banner config fetch/live-listen, dismiss tracking, feedback
│   └── FirebaseManager.kt       — Auth (email + Google), Analytics, Realtime DB backup/restore
├── tts/
│   ├── BundledTts.kt            — fallback: plays pre-recorded MP3s from res/raw when system TTS fails
│   ├── DeviceInfo.kt            — MIUI/manufacturer detection helpers
│   ├── SentenceAudioCache.kt    — generates+caches TTS audio files for sentences on demand
│   ├── TtsEngineSelector.kt     — picks best available system TTS engine
│   └── TtsManager.kt            — orchestrates system TTS → BundledTts → intent-fallback chain
└── ui/
    ├── AuthFragment.kt          — email + Google sign-up/sign-in screen
    ├── BaseFragment.kt          — provides `vm` (shared MainViewModel) to all fragments
    ├── GroupPracticeFragment.kt — practice mode for custom word groups
    ├── GroupsFragment.kt        — create/manage custom word groups
    ├── HomeFragment.kt          — HSK level cards, XP/streak/daily progress
    ├── MainViewModel.kt         — shared ViewModel, exposes LiveData + suspend functions
    ├── PracticeFragment.kt      — main flashcard drill (type pinyin/meaning)
    ├── SentenceFragment.kt      — sentence practice with audio playback
    ├── SettingsFragment.kt      — all settings: TTS, alarm, account, goal slider, day-start picker
    ├── StatsFragment.kt         — XP/streak/accuracy + 2 bar charts (words passed, time studied)
    └── TestFragment.kt          — scored quiz mode (words/sentences, two directions)

app/src/main/res/
├── raw/                         — ~330 pre-recorded MP3 audio files (words + sentences), named by
│                                   Unicode codepoint hex (w_XXXX.mp3) or sentence id (s_sN_M.mp3)
├── layout/
│   ├── activity_main.xml        — FrameLayout root; LinearLayout (nav host + bottom nav) + banner overlay
│   ├── view_banner.xml          — included banner card: image/video/feedback/action button
│   ├── activity_alarm.xml       — full-screen alarm UI (TextClock + Snooze/Stop buttons)
│   ├── fragment_auth.xml        — email/Google sign-in form
│   ├── fragment_settings.xml    — account card, alarm card (day-start + countdown), TTS card, goal slider
│   ├── fragment_stats.xml       — XP/streak cards, 2 BarChartView charts, level progress, recent list
│   └── ... (other fragment layouts)
├── drawable/ic_google.xml, ic_close.xml, ic_notification.*, pb_daily.xml, etc.
├── mipmap-*/ic_launcher*.png    — app icon (orange gradient, "学" character)
└── values/themes.xml            — AppTheme (dark) + AlarmTheme

app/google-services.json         — Firebase config (package_name MUST be com.iqra.chinese)
app/build.gradle                 — all dependencies (see §3)
generate_sentence_audio.py       — gTTS script to regenerate sentence audio (user runs locally)
BANNER_API_GUIDE.md              — full Postman/REST API guide for pushing banners
```

---

## 3. Dependencies (app/build.gradle)

```gradle
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
    id 'org.jetbrains.kotlin.kapt'
    id 'com.google.gms.google-services'
}

dependencies {
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0'
    implementation 'androidx.lifecycle:lifecycle-livedata-ktx:2.7.0'
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.7.0'
    implementation 'androidx.navigation:navigation-fragment-ktx:2.7.6'
    implementation 'androidx.navigation:navigation-ui-ktx:2.7.6'
    implementation 'androidx.room:room-runtime:2.6.1'
    implementation 'androidx.room:room-ktx:2.6.1'
    kapt 'androidx.room:room-compiler:2.6.1'
    implementation 'com.google.code.gson:gson:2.10.1'
    implementation 'androidx.cardview:cardview:1.0.0'
    implementation 'androidx.recyclerview:recyclerview:1.3.2'

    // Firebase BOM
    implementation platform('com.google.firebase:firebase-bom:32.7.4')
    implementation 'com.google.firebase:firebase-auth-ktx'
    implementation 'com.google.firebase:firebase-analytics-ktx'
    implementation 'com.google.firebase:firebase-database-ktx'

    // Google Sign-In — legacy GoogleSignInClient (NOT Credential Manager — see §7.3 why)
    implementation 'androidx.credentials:credentials:1.2.2'
    implementation 'androidx.credentials:credentials-play-services-auth:1.2.2'
    implementation 'com.google.android.libraries.identity.googleid:googleid:1.1.1'
    implementation 'com.google.android.gms:play-services-auth:21.2.0'

    // Image loading for remote banners
    implementation 'io.coil-kt:coil:2.6.0'

    // Inline video player for banners
    implementation 'androidx.media3:media3-exoplayer:1.3.1'
    implementation 'androidx.media3:media3-ui:1.3.1'
}
```

Root `build.gradle` additionally has: `id 'com.google.gms.google-services' version '4.4.1' apply false`

---

## 4. Feature-by-Feature Reference

### 4.1 Core Learning App (pre-existing before Firebase work)
- HSK 1–6 vocabulary (150/300/600/1200/2500/5000 words respectively) + ~20 example sentences
- Flashcard practice (type pinyin or meaning), test mode (scored, two directions), custom word groups
- XP, streak, achievements (13 total), level unlock at 80% mastery
- Room DB tracks per-word/sentence pass/fail history
- TTS with 3-layer fallback (see §4.5)

### 4.2 Firebase Authentication
**Files:** `firebase/FirebaseManager.kt`, `ui/AuthFragment.kt`, `res/layout/fragment_auth.xml`

- Email/password sign-up, sign-in, password reset (`FirebaseManager.signUp/signIn/sendPasswordReset`)
- Google Sign-In via **legacy `GoogleSignInClient`** (`FirebaseManager.getGoogleSignInClient` + `handleGoogleSignInResult`), NOT the modern Credential Manager API
  - **Why legacy, not Credential Manager:** Credential Manager fails with `BAD_AUTHENTICATION — Long live credential not available` on Xiaomi/MIUI debug builds. MIUI restricts Google account token access for non-Play-Store-installed apps. Legacy `GoogleSignInClient` works reliably.
  - `AuthFragment` uses `ActivityResultContracts.StartActivityForResult()` + `client.signOut()` before launching (forces account picker every time instead of auto-selecting)
- `WEB_CLIENT_ID` in `AuthFragment.kt` must be the OAuth `client_type: 3` entry from `google-services.json`
- Required Firebase Console setup: SHA-1 fingerprint added to the app, Email/Password + Google both enabled under Authentication → Sign-in method

### 4.3 Firebase Cloud Backup & Restore
**Files:** `firebase/FirebaseManager.kt`, `data/Repository.kt`, `data/Prefs.kt`, `ui/MainViewModel.kt`

- **Backup** (`backupStatsIfDue`): once-per-day (tracked via `Prefs.lastBackupDate`), pushes to:
  ```
  /users/{uid}/stats/      → xp, streak, bestTest, unlockedLevels, earnedAch, lastBackupDate, email
  /users/{uid}/attempts/   → per-word pass/fail/level/type/lastTs (itemId with "." replaced by "_")
  ```
- Triggered from `MainViewModel.startTimer()` loop (every tick, but internally no-ops if already backed up today) and manually via Settings "Sync Now" button
- **Restore** (`restoreStats`): called from `AuthFragment.onSuccess()` after every sign-in
  - Detects "fresh install" (`prefs.xp == 0 && prefs.streak == 0`) — on fresh install, restores stats AND replays the full attempts table into Room via `attemptDao.upsert()`
  - If not fresh install, still restores stats if `cloudXp > localXp`
  - Sets `lastBackupDate = today` after restore so the next backup doesn't immediately clobber the just-restored cloud data

**Required RTDB security rules:**
```json
{
  "rules": {
    "users": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    },
    "banners": {
      ".read": true,
      "active":   { ".write": false },
      "stats":    { ".write": true },
      "feedback": { ".write": true }
    }
  }
}
```

### 4.4 Two-Phase Hourly Study Alarm (clock-app-grade)
**Files:** `alarm/AlarmScheduler.kt`, `alarm/AlarmReceiver.kt`, `alarm/AlarmActivity.kt`, `alarm/AlarmSoundService.kt`, `alarm/AlarmPrefs.kt`

**Concept:** The user's "day" starts at a configurable time (default 00:01, settable in Settings via two `NumberPicker`s, 0–23h / 0–59m). Hours 0–11 of the day = Phase 0; hours 12–23 = Phase 1.

- **Phase 0 (0–12h):** Standard heads-up notification, one snooze allowed per phase per day
- **Phase 1 (12–24h):** Full clock-style alarm — `AlarmSoundService` (foreground service) starts, plays looping `TYPE_ALARM` ringtone + vibration pattern, and launches `AlarmActivity` (full-screen, `setShowWhenLocked`/`setTurnScreenOn`, shows over lock screen, back button disabled). User must tap Snooze (once/day) or "Study Now — Stop Alarm".
- Both phases silently no-op and reschedule if `dailySecs >= dailyGoalSecs` (the configurable goal, see §4.7)

**Critical implementation detail — why GUI wasn't showing initially:**
`ctx.startActivity()` called directly from a `BroadcastReceiver` is blocked by Android 10+ background-activity-start restrictions (MIUI enforces this strictly). Fix: `AlarmSoundService.onCreate()` calls `startActivity()` on **itself** (a running foreground service is trusted), plus a `setFullScreenIntent()` notification as the official Android fallback mechanism (requires `IMPORTANCE_HIGH` channel — `IMPORTANCE_LOW` silently suppresses full-screen intents).

**Scheduling:** Uses `AlarmManager.setAlarmClock()` (NOT `setExactAndAllowWhileIdle`) — same priority tier as the system Clock app, exempt from Doze mode, shows the alarm icon in status bar. `AlarmScheduler.scheduleDelay()` also writes `next_trigger_ms` to SharedPrefs so Settings can show a live countdown.

**MIUI Autostart:** On first launch, if `Build.MANUFACTURER == "xiaomi"`, shows a one-time dialog (`MainActivity.showMiuiAlarmPromptIfNeeded`) explaining Autostart is required for the alarm to survive app-kill, with a button that deep-links to `com.miui.securitycenter/.permcenter.autostart.AutoStartManagementActivity` (falls back to generic app-info page if that component doesn't exist).

**Known limitation:** Without Autostart enabled by the user, MIUI can still block the alarm if the app is fully swiped away from recents. This is an OS-level restriction with no code-only workaround.

### 4.5 TTS — 3-Layer Fallback Chain
**Files:** `tts/TtsManager.kt`, `tts/BundledTts.kt`, `tts/SentenceAudioCache.kt`, `tts/TtsEngineSelector.kt`, `tts/DeviceInfo.kt`

1. **System TTS** (Android `TextToSpeech` API) — tried first
2. **BundledTts** — if system TTS init fails (common on MIUI), looks up the exact text in `AUDIO_MAP` (a `Map<String, Int>` of text → `R.raw.*` resource id) and plays via `MediaPlayer`
   - ~310 word entries (filenames `w_<unicode-hex-codepoints>.mp3`)
   - 20 sentence entries (filenames `s_s<level>_<index>.mp3`, e.g. `s_s1_1.mp3`)
3. **Intent-based fallback** — last resort, fires an Android TTS intent for the OS to handle externally

**Sentence audio generation history:** Initially silent placeholders were used to keep the build compiling. Real audio was then generated using a **from-scratch Klatt-style formant synthesiser** (pure Python/numpy/scipy, no network/model downloads) — maps each Chinese character to pinyin (initial+rhyme+tone) via a hardcoded `CHAR_PINYIN` dict, synthesises vowels via resonator filters tuned to Mandarin formant frequencies, consonants via noise/burst/nasal models, and applies the 4 Mandarin tone F0 contours. Output encoded to MP3 via FFmpeg at 56kbps/22050Hz/mono (matching the original word-audio quality). This is a one-time fallback approach — if network/real TTS becomes available in a future session, regenerating via `generate_sentence_audio.py` (uses `gTTS`, requires `pip install gtts` + internet) produces much higher quality audio.

`SentenceAudioCache` separately handles **on-demand** TTS generation+caching for sentences not in the bundled map (used by `SentenceFragment.preCacheLevel()` to pre-warm audio in the background when a level is opened).

### 4.6 Remote Banner / Announcement System
**Files:** `firebase/BannerManager.kt`, `MainActivity.kt` (displayBanner, showBannerIfAvailable), `res/layout/view_banner.xml`

Admin pushes banner config via Postman `PUT` to Firebase RTDB REST API (no Cloud Functions needed) — see `BANNER_API_GUIDE.md` for full reference. Schema at `/banners/active`:

```json
{
  "id": "unique_id",
  "title": "Headline",
  "message": "Body text",
  "imageUrl": "https://... (optional)",
  "videoUrl": "https://... (optional — YouTube or direct .mp4)",
  "actionLabel": "Button text (optional)",
  "actionUrl": "https://... (optional)",
  "feedback": false,
  "feedbackQuestion": "Optional question shown above feedback input",
  "enabled": true
}
```

**Live delivery (no app restart needed):** `BannerManager.startLiveListener()` attaches a Firebase `ValueEventListener` to `/banners/active` — fires immediately on attach (handles initial launch) AND again any time the admin pushes an update while the app is open. `MainActivity` tracks `lastShownBannerId` to avoid re-showing/flickering on redundant Firebase resends. If `enabled` flips to `false` while a banner is showing, it auto-dismisses live.

**Video handling:**
- If `videoUrl` contains `youtube.com`/`youtu.be` → shows YouTube thumbnail (auto-fetched from `img.youtube.com/vi/{id}/hqdefault.jpg`) + red "▶ Watch on YouTube" button that opens the YouTube app/browser
- Otherwise (direct `.mp4` etc.) → plays inline via ExoPlayer (`androidx.media3`) embedded in the banner card

**Feedback form:** if `feedback: true`, shows a multiline `TextInputEditText` + submit button below the content. Submission writes to `/banners/feedback/{bannerId}/{uid_or_anon}` with `{response, uid, ts}`. Read via Postman GET.

**Dismiss tracking:** tapping X writes to `/banners/stats/{bannerId}/dismissCount` (read-increment-write pattern), and stores the dismissed id locally (`SharedPreferences`) so the same banner never reshows on that device. Tapping the dark scrim outside the card dismisses for that session only (reshows next launch).

**Required RTDB rules:** see §4.3 above (banners node).

**Layout structure (`view_banner.xml`):** root `FrameLayout` (id `bannerOverlay`, dark scrim) → `ScrollView` → `CardView` → `ImageView` (image) / `PlayerView` (video) → title/message TextViews → YouTube button → action button → feedback section (question label, input, submit, thank-you message) → close (X) button.

**ViewBinding gotcha:** the `<include>` tag in `activity_main.xml` MUST have `android:id="@+id/bannerInclude"` for ViewBinding to expose nested views as `binding.bannerInclude.xxx` — without an id on the include, nested views are NOT accessible from the parent binding at all (this caused a build failure during development).

### 4.7 Configurable Daily Study Goal (5–60 min, default 30)
**Files:** `data/Prefs.kt` (`dailyGoalMinutes`, `dailyGoalSecs`), `res/layout/fragment_settings.xml` (SeekBar), `ui/SettingsFragment.kt`

- `Prefs.dailyGoalMinutes` clamped 5–60, stored as `goal_min` int in SharedPrefs (same "iqra" file `AlarmReceiver` reads directly via `getSharedPreferences("iqra", ...)`)
- `SeekBar` in Settings (`android:min="5" android:max="60"`, requires minSdk 26 — matches project's minSdk exactly) updates live label, saves on `onStopTrackingTouch`
- **All 5 places that previously hardcoded `1800` seconds now reference the configurable goal:** `Repository.checkAchievements()`, `AlarmReceiver.handleAlarm()`, `StatsFragment`, `SettingsFragment`, `HomeFragment`

### 4.8 Time-Studied Tracking & Chart
**Files:** `data/Prefs.kt` (`timeHistoryJson`, `getTimeHistory()`, `saveTimeForDate()`), `data/Repository.kt` (`touchStreak`, `tickDaily`), `ui/StatsFragment.kt`, `res/layout/fragment_stats.xml`

- `Prefs.timeHistoryJson` stores `{"yyyy-MM-dd": totalSecondsThatDay}` as a Gson-serialized map
- `Repository.touchStreak()` saves yesterday's final `dailySecs` total to history right before resetting it at day-rollover
- `Repository.tickDaily()` also throttle-saves today's running total every 10 seconds (`dailySecs % 10 == 0`) so the chart reflects live progress, not just completed days
- StatsFragment renders a second `BarChartView` ("TIME STUDIED (MIN) – LAST 7 DAYS") alongside the existing "WORDS PASSED" chart, plus a total-time-studied summary line

### 4.9 Custom App Icon
Generated from a user-uploaded JPG (orange gradient rounded square with "学" character) into all 5 mipmap densities (mdpi 48px → xxxhdpi 192px) for both `ic_launcher.png` and `ic_launcher_round.png`.

---

## 5. Known Conventions & Gotchas

1. **ViewBinding + `<include>`:** any included layout needs `android:id` on the `<include>` tag itself to expose nested views in the parent's generated binding class.
2. **XML namespace declarations** (`xmlns:app`, `xmlns:tools`) must be on the ROOT element only — duplicating them on child elements causes AAPT parse errors (`AttributePrefixUnbound`).
3. **Theme window attributes** (`windowShowOnLockScreen`, `windowTurnScreenOn`, `windowKeepScreenOn`) are NOT valid `<style>` XML attributes — they don't exist as `android:attr` resources. Must be set programmatically via `Activity.setShowWhenLocked()` / `setTurnScreenOn()` (API 27+) or `WindowManager.LayoutParams` flags (older).
4. **MIUI background restrictions** are the recurring theme of this project — wherever something "doesn't work on the test device," check: (a) is it a `BroadcastReceiver` trying to start an Activity directly (blocked, use a foreground Service instead), (b) is Autostart/battery-optimization exemption needed, (c) is the notification channel importance high enough for full-screen intents to fire.
5. **Firebase package name must match exactly** — `google-services.json`'s `package_name` field must equal the app's `applicationId`/`namespace` (`com.iqra.chinese`), or all Firebase services silently fail to initialize correctly ("internal error" on sign-up was caused by this mismatch during development).
6. **Copyright/IP note for future audio work:** the formant-synthesised sentence audio is original, code-generated, low-fidelity but functional. If a future session has network access, prefer regenerating via `gTTS` (see `generate_sentence_audio.py`) for natural-sounding audio instead.
7. **All Repository methods that touch Firebase are suspend functions** called from `viewModelScope.launch` in `MainViewModel` — never call Firebase/Room directly from a Fragment without going through the ViewModel/Repository layer.
8. **Output delivery convention used throughout this project's sessions:** after any code change, repackage the FULL project (not just changed files) via:
   ```
   cd /home/claude/iqra && zip -r /mnt/user-data/outputs/IqraChinese-Firebase.zip iqra-final/ \
     --exclude "*/build/*" --exclude "*/.gradle/*" --exclude "*.class" --exclude "*.dex" -q
   ```
   then `present_files` on the zip. User builds locally in Android Studio (Windows) and reports back compiler errors or logcat output for the AI to diagnose.

---

## 6. Firebase Console Setup Checklist (for a fresh project clone)

1. Create Firebase project, add Android app with package `com.iqra.chinese`
2. Add SHA-1 debug fingerprint (`keytool -list -v -keystore %USERPROFILE%\.android\debug.keystore -alias androiddebugkey -storepass android -keypass android`)
3. Download `google-services.json`, place in `app/`
4. Authentication → Sign-in method → enable **Email/Password** and **Google**
5. Realtime Database → Create Database → apply rules from §4.3
6. Copy the OAuth `client_type: 3` (Web client) id from `google-services.json` into `AuthFragment.WEB_CLIENT_ID`
7. (Optional) Get a Database Secret for Postman admin pushes: Project settings → Service accounts → Database secrets

---

## 7. Things NOT Yet Implemented (potential future asks)

- Real (non-synthesized) sentence TTS audio — requires running `generate_sentence_audio.py` with network access
- Push notifications (FCM) — only Realtime Database polling/listening is used currently, no FCM integration
- Admin web dashboard for banners (currently Postman-only)
- Localisation/i18n (app is English-only UI with Chinese learning content)
- Dark/light theme toggle (app is dark-theme-only currently)
- Widget/home-screen shortcut for quick practice
- Offline-first conflict resolution beyond simple "higher XP wins" (no merge strategy for attempts made offline on two devices)

---

*Document generated to preserve full project context across Claude sessions. Keep this updated by asking Claude to append a summary after major feature additions.*
