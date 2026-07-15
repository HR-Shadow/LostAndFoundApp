# Firebase Setup for Lost & Found App

Before running the app, connect it to your Firebase project.

## 1. Create a Firebase project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click **Add project** and follow the wizard
3. Name it e.g. `Lost Found App`

## 2. Add Android app

1. In Firebase Console → **Project settings** → **Your apps** → **Add app** → **Android**
2. Package name: `eu.tutorials.lostfoundapp` (must match exactly)
3. Download `google-services.json`
4. **Replace** `app/google-services.json` in this project with the downloaded file

## 3. Enable Firebase services

In Firebase Console, enable:

| Service | Where to enable |
|---------|-----------------|
| **Authentication** | Build → Authentication → Sign-in method → **Email/Password** → Enable |
| **Firestore** | Build → Firestore Database → Create database (start in test mode for dev) |
| **Storage** | Build → Storage → Get started |
| **Cloud Messaging** | Project settings → Cloud Messaging (enabled by default) |

## 4. Firestore security rules (Step 2 — item reports)

Update rules in **Firestore → Rules** to allow authenticated users to create/read their own reports:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    match /lost_items/{itemId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null
        && request.resource.data.userId == request.auth.uid;
      allow update, delete: if request.auth != null
        && resource.data.userId == request.auth.uid;
    }
    match /found_items/{itemId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null
        && request.resource.data.userId == request.auth.uid;
      allow update, delete: if request.auth != null
        && resource.data.userId == request.auth.uid;
    }
    match /match_requests/{matchId} {
      allow read: if request.auth != null
        && request.auth.uid in resource.data.participants;
      allow create: if request.auth != null;
      allow update: if request.auth != null
        && request.auth.uid in resource.data.participants;
    }
  }
}
```

## 5. Firestore composite indexes (Step 3 — matching)

Firebase may prompt you to create indexes when matching runs. You can also add them manually under **Firestore → Indexes**:

| Collection | Fields |
|------------|--------|
| `lost_items` | `category` Asc, `status` Asc |
| `found_items` | `category` Asc, `status` Asc |
| `match_requests` | `participants` Array-contains, `timestamp` Desc |

## 6. Storage security rules (photo uploads)

Update rules in **Storage → Rules**:

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /item_images/{type}/{userId}/{fileName} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

## 7. Sync and run

1. Open the project in Android Studio
2. **File → Sync Project with Gradle Files**
3. Run on emulator or device
4. Test **Sign Up** / **Sign In**
5. Report a **lost** item on one account and a similar **found** item on another
6. Tap **Possible Matches** on Home to review and confirm

## Verify in Firebase Console

After submitting reports, check:

- **Firestore → `lost_items`** or **`found_items`** — new documents
- **Firestore → `match_requests`** — auto-created when scores exceed 45%
- **Storage → `item_images/`** — uploaded photos (if any)

## Test matching (two accounts)

Use two emulator instances or one emulator + one physical device:

1. **Account A** — Report Lost: e.g. "Black Wallet", category Wallet, location "Central Mall", details "scratch on back"
2. **Account B** — Report Found: same category, similar name/location/description
3. Both accounts tap **Possible Matches** — each should see the match
4. Both tap confirm — status becomes `confirmed`

## Troubleshooting

- **CONFIGURATION_NOT_FOUND**: Replace `google-services.json` with your real file from Firebase
- **PERMISSION_DENIED** on submit: Update Firestore and Storage rules (sections 4 & 6)
- **FAILED_PRECONDITION** (index): Create composite indexes from the error link (section 5)
- **Email already in use**: Use a different email or sign in instead
- **Network error**: Check internet permission and emulator network
