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

## 6. View/Dismiss Tracking

Every time a user dismisses (taps X on) a banner, the app increments a counter at:
```
/banners/stats/{bannerId}/dismissCount
```

To check how many times a banner was dismissed:
```
GET https://iqra-chinese-default-rtdb.firebaseio.com/banners/stats/promo_2026_07/dismissCount.json
```

**Required rules update** — add this so the app can write dismiss counts:
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
      "stats": { ".write": true }
    }
  }
}
```

