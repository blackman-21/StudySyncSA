# StudySync SA

StudySync SA is a modern Android application designed for students to manage their tasks and notes efficiently. The app provides a seamless user experience with offline support, secure authentication, and real-time synchronization.

## Features

- **Task Management**: Create, view, and manage daily study tasks.
- **Notes**: Organize thoughts and study materials with a dedicated notes section.
- **Authentication**: Secure user access (integrated with Firebase).
- **Offline First**: Built with Room persistence to ensure data is available without internet.
- **Dynamic Theming**: Supports Light, Dark, and System-based Material 3 themes.
- **Background Sync**: Uses WorkManager to keep data synchronized with the backend.

## Tech Stack

- **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose) for a modern, declarative UI.
- **Navigation**: [Jetpack Navigation](https://developer.android.com/guide/navigation) for Compose.
- **Architecture**: MVVM (Model-View-ViewModel) pattern.
- **Database**: [Room](https://developer.android.com/training/data-storage/room) for local data persistence.
- **Networking**: [Retrofit](https://square.github.io/retrofit/) & OkHttp for API communication.
- **Serialization**: Kotlinx Serialization.
- **Background Tasks**: [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) for data synchronization.
- **Dependency Management**: Gradle with Version Catalog.

## Getting Started

### Prerequisites

- Android Studio Koala or newer.
- Android SDK 34+.
- Kotlin 2.0.0+.

### Installation

1. Clone the repository:
2. Open the project in Android Studio.
3. Sync the project with Gradle files.
4. (Optional) Set up your `google-services.json` in the `app/` directory if Firebase features are enabled.
5. Build and run the app on an emulator or physical device.

## Project Structure

- `data/`: Contains the local database (Room), API services (Retrofit), and Repository implementations.
- `ui/`: Contains Compose screens, reusable components, and theme definitions.
- `viewmodel/`: Contains ViewModels that hold UI state and interact with the Repository.
- `navigation/`: Defines the app's navigation graph and destinations.

## Contributing

1. Fork the Project.
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`).
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`).
4. Push to the Branch (`git push origin feature/AmazingFeature`).
5. Open a Pull Request.

## License

Distributed under the MIT License. See `LICENSE` for more information.
