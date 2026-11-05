# StudySync SA 🎓 - Part 3 Final PoE Submission

[![Android Build & Test CI](https://github.com/studysync/studysync-sa/actions/workflows/build.yml/badge.svg)](https://github.com/studysync/studysync-sa/actions/workflows/build.yml)

**StudySync SA** is a full-featured, offline-first mobile study management application specifically engineered for South African higher-education students. It empowers students with robust task management, rich multimedia study notes, real-time cloud synchronization, multi-language support (English, isiZulu, Afrikaans, isiXhosa), and secure credential protection.

---

## 🚀 Key Features Implemented (Part 3 Requirements)

### 1. 🔐 Encrypted Password Authentication & Single Sign-On (SSO)
- **Salted SHA-256 Password Encryption**: Sensitive passwords are never saved in plaintext. Uses a 16-byte random salt combined with SHA-256 hashing via `PasswordEncoder`.
- **Institutional Single Sign-On (SSO)**: Seamless single click authentication with Google / Institutional SSO credentials using `SsoManager`.
- **Validation & Error Resilience**: Validates email format and password complexity. Displays user-friendly error banners without crashing.

### 2. 🗄️ NoSQL Database Store & Real-Time Sync
- **Cloud Firestore Document Store**: Read and write capabilities for tasks, notes, user profiles, and sync operations in a NoSQL document database (`NoSqlSyncManager`).
- **Real-Time Data Access**: Reads and writes to NoSQL collections (`/tasks`, `/notes`, `/users`).

### 3. 📦 Blob Storage for Media & Attachments
- **Firebase Blob Storage & Local Cache**: Dedicated `BlobStorageManager` for uploading and downloading binary study assets (images, camera snaps, gallery documents, profile avatars).
- **Note Image Attachments**: Students can attach gallery photos or camera pictures to study notes with full thumbnail previews and detailed media views.

### 4. 🔄 Offline Mode with WorkManager Synchronization
- **Offline-First Persistence**: Powered by Room DB (`StudySyncDatabase`, `StudySyncDao`). All user actions (creating tasks, editing notes, updating profile) work 100% offline.
- **Automated SyncWorker**: Uses `WorkManager` with exponential backoff and network constraints to automatically push pending local operations to both the REST API and NoSQL DB once internet connection is restored.

### 5. 🔔 Real-Time Push Notifications & Alerts
- **Firebase Cloud Messaging (FCM)**: Remote push notification service (`StudySyncMessagingService`) to deliver real-time notifications.
- **Local Notification Manager**: Handles high-priority notification channels (`LocalNotificationManager`) for task deadline reminders, study alerts, and cloud sync status feedback.

### 6. 🇿🇦 Multi-Language Support (South African Languages)
- Full support for four South African official languages:
  - 🇬🇧 **English** (`values/strings.xml`)
  - 🇿🇦 **isiZulu** (`values-zu/strings.xml`)
  - 🇿🇦 **Afrikaans** (`values-af/strings.xml`)
  - 🇿🇦 **isiXhosa** (`values-xh/strings.xml`)
- **Dynamic Language Switcher**: Integrated in `SettingsScreen` and powered by `LocaleHelper` to switch app text in real-time.

### 7. 🛍️ Play Store Publication Readiness
- **Release Build Configuration**: Minified and optimized build type with ProGuard / R8 rules (`proguard-rules.pro`).
- **Branded App Assets**: Branded adaptive launcher icons (`ic_launcher`, `ic_launcher_round`, vector background/foreground drawables).
- **Manifest Hygiene**: Explicit permissions (`INTERNET`, `POST_NOTIFICATIONS`, `CAMERA`), notification service declarations, and exported activity flags.

---

## 🏗️ Architecture & Technology Stack

```
com.studysync.sa/
├── MainActivity.kt                 # Single Activity with Compose Navigation
├── data/
│   ├── local/
│   │   ├── StudySyncDatabase.kt   # Room Database Definition
│   │   ├── SessionManager.kt      # EncryptedSharedPreferences Manager
│   │   ├── dao/
│   │   │   └── StudySyncDao.kt    # Room Data Access Object
│   │   └── entity/                # Room Entities (User, Task, Note, Module, SyncOperation)
│   ├── remote/
│   │   ├── StudySyncApiService.kt # Retrofit REST API interface
│   │   ├── NoSqlSyncManager.kt   # Cloud Firestore NoSQL Database Client
│   │   └── BlobStorageManager.kt  # Firebase Storage & Local Blob Manager
│   └── repository/
│       └── StudySyncRepository.kt # Offline-First Repository
├── notification/
│   ├── LocalNotificationManager.kt# System Notification Channels & Alerts
│   └── StudySyncMessagingService.kt # FCM Push Notification Service
├── ui/
│   ├── navigation/                 # Navigation Bar & Screen Destinations
│   ├── screens/                    # Jetpack Compose Screens (Home, Tasks, Notes, Settings, Auth, Welcome)
│   └── theme/                      # Material 3 Color, Type & Theme Definitions
├── util/
│   ├── PasswordEncoder.kt          # Salted SHA-256 Hashing & Validation
│   ├── SsoManager.kt               # Google / Institutional SSO Provider
│   └── LocaleHelper.kt             # South African Language Context Wrapper
├── viewmodel/                      # ViewModel Layer (TaskViewModel, NoteViewModel)
└── worker/
    └── SyncWorker.kt               # WorkManager Background Offline Sync
```

---

## ⚙️ Automated Testing & GitHub Actions CI/CD

This repository includes automated Continuous Integration via GitHub Actions defined in `.github/workflows/build.yml`.

### CI Workflow Pipeline
1. **Triggers**: On every push and pull request to `main` / `master`.
2. **Environment**: Ubuntu Latest with JDK 17.
3. **Execution Steps**:
   - Checks out project files.
   - Sets up Java 17 Temurin JDK with Gradle caching.
   - Runs unit tests: `./gradlew testDebugUnitTest`.
   - Assembles Debug APK: `./gradlew assembleDebug`.
   - Uploads compiled APK as a build artifact (`study-sync-debug-apk`).

---

## 📝 Release Notes (Prototype to Final PoE Submission)

### Version 1.0.0 Final Release
- **Added Salted SHA-256 Password Hashing**: Upgraded authentication logic to encrypt all credentials before storing locally or remotely.
- **Added Google Single Sign-On (SSO)**: Integrated SSO authentication option in `AuthScreen`.
- **Added NoSQL Database Integration**: Integrated Cloud Firestore NoSQL store (`NoSqlSyncManager`) for real-time document read/write.
- **Added Blob Storage Attachment Feature**: Added media blob upload/download in `NotesScreen` and `SettingsScreen` for note image attachments and profile picture uploads.
- **Added Real-time Push Notification Alerts**: Implemented FCM `StudySyncMessagingService` and `LocalNotificationManager` for due dates and sync alerts.
- **Added Multi-language Support**: Full support for English, isiZulu, Afrikaans, and isiXhosa with live dynamic language switching.
- **Added Automated Testing & CI/CD**: Added unit tests for ViewModels, PasswordEncoder, and Repositories (`12/12 passing`), plus `.github/workflows/build.yml` for automated GitHub builds.
- **Play Store Hardening**: Added ProGuard keep rules, adaptive launcher icon vector drawables, release build configuration, and Android 13+ notification permissions.

---

## 🎥 Demonstration Video Instructions

When recording the final demonstration video on a physical mobile device:
1. **User Registration & Login**: Show registering a new user with password validation and logging in.
2. **Single Sign-On (SSO)**: Demonstrate tapping the Google SSO button.
3. **Tasks & Offline Sync**: Create a task while offline, verify local display, enable network, trigger **Sync Now**, and show notification alert.
4. **Blob Storage Attachment**: Add a study note with a camera photo attachment, and view the image preview.
5. **Multi-Language Switch**: Open Settings, switch language to **isiZulu** or **Afrikaans**, and verify translated UI text.
6. **NoSQL & REST Data Verification**: Briefly show Firestore document console or API response payload logs.

---

## 📜 License

Distributed under the MIT License.
