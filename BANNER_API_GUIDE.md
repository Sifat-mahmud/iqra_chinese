# Pushing Banners via Postman (Firebase Realtime Database REST API)

Firebase Realtime Database exposes a REST API automatically — no Cloud Functions needed.
Any path becomes a JSON endpoint by appending `.json`.

## 1. Get your Database Secret (one-time setup)

This secret lets Postman write to the database, bypassing the security rules that protect user data.

1. Go to **Firebase Console** → ⚙️ **Project settings** → **Service accounts** tab
2. Click **Database secrets** (near the bottom — legacy section)
3. Click **Show** next to the existing secret, or **Add secret** if none exists
4. Copy the long string — this is your `DATABASE_SECRET`

⚠️ Keep this secret private — anyone with it has full read/write access to your database.

## 2. Postman Request — Push/Update Banner

**Method:** `PUT`

**URL:**
```
https://iqra-chinese-default-rtdb.firebaseio.com/banners/active.json?auth=YOUR_DATABASE_SECRET
```

**Headers:**
```
Content-Type: application/json
```

**Body (raw JSON):**
```json
{
  "id": "promo_2026_07",
  "title": "🎉 New HSK 6 Words Added!",
  "message": "We've added 500 new vocabulary words to HSK 6. Tap to check them out!",
  "imageUrl": "",
  "actionLabel": "",
  "actionUrl": "",
  "enabled": true
}
```

Click **Send**. A `200 OK` with the same JSON echoed back means success.

The banner will appear to users next time they open the app (or are online and the
app fetches it again).

## 3. Common Operations

### Update banner text (keep same id — won't re-show to users who dismissed it)
Same `PUT` request as above with `id` unchanged, just edit `title`/`message`.

### Push a NEW banner (re-shows to everyone, including those who dismissed the old one)
Same `PUT` request, but change the `id` value to something new, e.g. `"promo_2026_07_v2"`.

### Hide the banner for everyone instantly
```
PUT https://iqra-chinese-default-rtdb.firebaseio.com/banners/active/enabled.json?auth=YOUR_DATABASE_SECRET
```
Body:
```json
false
```

### Add an image + action button
```json
{
  "id": "promo_2026_07",
  "title": "🎉 New HSK 6 Words Added!",
  "message": "We've added 500 new vocabulary words to HSK 6.",
  "imageUrl": "https://your-cdn.com/banner-image.png",
  "actionLabel": "View on Play Store",
  "actionUrl": "https://play.google.com/store/apps/details?id=com.iqra.chinese",
  "enabled": true
}
```

### Delete the banner entirely
**Method:** `DELETE`
```
https://iqra-chinese-default-rtdb.firebaseio.com/banners/active.json?auth=YOUR_DATABASE_SECRET
```

### Read current banner (GET — no auth needed if rules allow public read on /banners)
```
GET https://iqra-chinese-default-rtdb.firebaseio.com/banners/active.json
```

## 4. Field Reference

| Field         | Required | Description                                              |
|---------------|----------|-----------------------------------------------------------|
| `id`          | ✅       | Unique string. Change it to re-show to users who dismissed |
| `title`       | ✅       | Bold headline shown at top of card                        |
| `message`     | ✅       | Body text                                                  |
| `imageUrl`    | optional | Public image URL, shown above the text (160dp height)      |
| `actionLabel` | optional | Button text — only shown if both label AND url are set     |
| `actionUrl`   | optional | URL opened in browser when button tapped                    |
| `enabled`     | ✅       | `true`/`false` (boolean, not string) — `false` hides banner globally |

## 5. Notes

- App fetches the banner on every launch (`MainActivity.onCreate`)
- Result is cached locally — works offline using last-fetched banner
- User dismissing via **X** stores the `id` locally; same `id` won't show again on that device
- User tapping outside the card dismisses for that session only (shows again next launch)

## 7. Video Banner Examples

### YouTube banner (Option A — thumbnail + opens YouTube)
```json
{
  "id": "video_promo_001",
  "title": "🎬 Watch: How to Study HSK",
  "message": "Check out our new tutorial on mastering HSK vocabulary fast!",
  "videoUrl": "https://youtu.be/YOUR_VIDEO_ID",
  "actionLabel": "",
  "actionUrl": "",
  "feedback": false,
  "enabled": true
}
```
App auto-extracts the YouTube thumbnail and shows a red "▶ Watch on YouTube" button.

### Inline video banner (Option B — plays inside the popup)
```json
{
  "id": "inline_video_001",
  "title": "📹 Quick HSK Tip",
  "message": "Watch this 30-second tip to boost your score!",
  "videoUrl": "https://your-cdn.com/tip-video.mp4",
  "feedback": false,
  "enabled": true
}
```
Any non-YouTube direct video URL (`.mp4`, `.webm`, etc.) plays inline in the banner card.

## 8. Feedback Form Examples

### Simple feedback banner
```json
{
  "id": "feedback_june_2026",
  "title": "💬 Quick Question",
  "message": "Help us improve Iqra Chinese!",
  "feedback": true,
  "feedbackQuestion": "What feature would you like us to add next?",
  "enabled": true
}
```

### Combined: image + feedback
```json
{
  "id": "survey_001",
  "title": "📊 We want your opinion!",
  "message": "Tell us how you're finding the app.",
  "imageUrl": "https://drive.google.com/uc?export=view&id=YOUR_FILE_ID",
  "feedback": true,
  "feedbackQuestion": "How would you rate your learning experience so far?",
  "actionLabel": "See all features",
  "actionUrl": "https://yourwebsite.com/features",
  "enabled": true
}
```

### Reading feedback responses (Postman GET)
```
GET https://iqra-chinese-default-rtdb.firebaseio.com/banners/feedback/feedback_june_2026.json?auth=YOUR_DATABASE_SECRET
```
Returns all responses keyed by user uid (or "anon" for non-logged-in users):
```json
{
  "uid123": { "response": "More sentence examples!", "uid": "uid123", "ts": 1718123456789 },
  "anon":   { "response": "Love the app!", "uid": "anon", "ts": 1718123400000 }
}
```


Every time a user dismisses (taps X on) a banner, the app increments a counter at:
```
/banners/stats/{bannerId}/dismissCount
```

To check how many times a banner was dismissed:
```
GET https://iqra-chinese-default-rtdb.firebaseio.com/banners/stats/promo_2026_07/dismissCount.json
```

**Required rules update** — add this so the app can write dismiss counts and feedback:
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
      "active": { ".write": false },
      "stats":    { ".write": true },
      "feedback": { ".write": true }
    }
  }
}
```

