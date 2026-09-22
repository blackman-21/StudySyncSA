Purpose

StudySync SA is designed to help college and university students manage academic modules, tasks, notes, deadlines, reminders and study progress in one mobile workspace.

Design and technology

The project design carries forward the Part 1 architecture:

Android client: Kotlin, Android Studio and Jetpack Compose

Authentication: Firebase Authentication, including Google sign-in

Local persistence: Room over SQLite

Background work: WorkManager

Backend: Kotlin/Ktor REST API, JSON endpoints under /api/v1

Server database: PostgreSQL

Push notifications: Firebase Cloud Messaging (FCM)

Languages: English, isiXhosa and Afrikaans

Main screens

Home: today’s priorities, weekly progress and synchronisation status

Tasks: module-linked tasks, priority, due date, reminders and completion

Notes: module-linked notes, labels and search

Profile/Settings: language, theme, notification preferences and study goals

Key functionality

Planned prototype flows include registration/login, Google SSO, settings, module/task/note management, REST connectivity, offline-first edits with a pending sync queue, and selected progress/reminder features.

Offline synchronisation design

Save task/note changes to Room immediately.

Queue offline operations as PENDING.

Use WorkManager to process the queue when connectivity returns.

Send operations to POST /api/v1/sync.

Authenticate and validate ownership/version on the server.

Mark successful operations SYNCED; retry temporary failures and report permanent errors.

Show Synced, Waiting to sync or Sync failed in the UI.

Security

Firebase Authentication is responsible for credentials. Plaintext passwords must never be stored in the app database or backend. API traffic must use HTTPS and authenticated requests; the server must enforce ownership checks. Tokens and diagnostic logs must be handled without exposing secrets.

API outline

Method

Endpoint

Purpose

POST

/api/v1/profile

Create/update profile

GET / POST

/api/v1/modules

List/create modules

PUT

/api/v1/modules/{id}

Update module

GET / POST

/api/v1/tasks

List/create tasks

PUT / DELETE

/api/v1/tasks/{id}

Update/delete task

GET / POST

/api/v1/notes

List/create notes

PUT

/api/v1/notes/{id}

Update note

POST

/api/v1/sync

Upload queued offline operations

Testing and GitHub Actions

Automated tests should cover input validation, data/repository operations, queue status transitions, retry/conflict handling and important ViewModel logic. UI tests should cover authentication, task/note workflows, offline-to-online synchronisation and settings. Configure GitHub Actions to run tests and assemble the Android app on pushes and pull requests.



AI-use disclosure

If AI tools were used, add an accurate disclosure (maximum 500 words) explaining what they assisted with, what you reviewed or changed, and citations required by the assessment. Remove this section only if it is not applicable.
