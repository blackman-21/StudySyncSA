package com.studysync.sa

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import androidx.room.Room
import com.google.firebase.messaging.FirebaseMessaging
import com.studysync.sa.data.local.SessionManager
import com.studysync.sa.data.local.StudySyncDatabase
import com.studysync.sa.data.repository.StudySyncRepository
import com.studysync.sa.ui.navigation.Screen
import com.studysync.sa.ui.navigation.bottomNavItems
import com.studysync.sa.ui.screens.*
import com.studysync.sa.ui.theme.StudySyncSATheme
import com.studysync.sa.util.LocaleHelper
import com.studysync.sa.viewmodel.NoteViewModel
import com.studysync.sa.viewmodel.TaskViewModel

class MainActivity : ComponentActivity() {

    private lateinit var sessionManager: SessionManager

    override fun attachBaseContext(newBase: Context) {
        try {
            val prefs = newBase.getSharedPreferences("study_sync_settings", Context.MODE_PRIVATE)
            val lang = prefs.getString("app_language", "English") ?: "English"
            val langCode = LocaleHelper.getLanguageCode(lang)
            val localizedContext = LocaleHelper.applyLocale(newBase, langCode)
            super.attachBaseContext(localizedContext)
        } catch (e: Exception) {
            Log.e("MainActivity", "Error attaching localized context", e)
            super.attachBaseContext(newBase)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(applicationContext)

        // Request runtime notification permission for Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        // Retrieve Firebase Cloud Messaging registration token
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    Log.d("MainActivity", "FCM Registration Token: $token")
                }
            }
        } catch (e: Exception) {
            Log.w("MainActivity", "Failed to retrieve FCM token", e)
        }

        val db = Room.databaseBuilder(
            applicationContext,
            StudySyncDatabase::class.java, "studysync_db"
        )
            .fallbackToDestructiveMigration()
            .build()

        val repository = StudySyncRepository(applicationContext, db.studySyncDao())

        enableEdgeToEdge()
        setContent {
            var appThemeSetting by remember { mutableStateOf("System") }
            var appLanguage by remember { mutableStateOf(sessionManager.getLanguage().ifBlank { "English" }) }

            val darkTheme = when (appThemeSetting) {
                "Light" -> false
                "Dark" -> true
                else -> isSystemInDarkTheme()
            }

            StudySyncSATheme(darkTheme = darkTheme) {
                MainApp(
                    repository = repository,
                    currentTheme = appThemeSetting,
                    currentLanguage = appLanguage,
                    onThemeChange = { appThemeSetting = it },
                    onLanguageChange = { newLang ->
                        appLanguage = newLang
                        sessionManager.setLanguage(newLang)
                        recreate()
                    }
                )
            }
        }
    }
}

@Composable
fun MainApp(
    repository: StudySyncRepository,
    currentTheme: String,
    currentLanguage: String,
    onThemeChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val taskViewModel: TaskViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = TaskViewModel(repository) as T
    })
    val noteViewModel: NoteViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = NoteViewModel(repository) as T
    })

    var showWelcomeScreen by remember { mutableStateOf(true) }
    var isAuthenticated by remember { mutableStateOf(false) }

    // User profile state saved upon registration / login
    var userName by remember { mutableStateOf("Student") }
    var userSurname by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("student@example.com") }
    var userAvatar by remember { mutableStateOf<Bitmap?>(null) }

    if (showWelcomeScreen) {
        WelcomeScreen(onGetStarted = { showWelcomeScreen = false })
    } else if (!isAuthenticated) {
        AuthScreen(
            repository = repository,
            onAuthSuccess = { name, surname, email, bitmap ->
                userName = name
                userSurname = surname
                userEmail = email
                if (bitmap != null) userAvatar = bitmap
                isAuthenticated = true
            }
        )
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        val title = when (screen.route) {
                            Screen.Home.route -> stringResource(R.string.nav_home)
                            Screen.Tasks.route -> stringResource(R.string.nav_tasks)
                            Screen.Notes.route -> stringResource(R.string.nav_notes)
                            Screen.Settings.route -> stringResource(R.string.nav_settings)
                            else -> screen.title
                        }
                        NavigationBarItem(
                            icon = { Icon(screen.icon!!, contentDescription = title) },
                            label = { Text(title) },
                            selected = currentDestination?.route == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController, 
                startDestination = Screen.Home.route, 
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Home.route) { HomeScreen(taskViewModel, userName) }
                composable(Screen.Tasks.route) { TasksScreen(taskViewModel) }
                composable(Screen.Notes.route) { NotesScreen(noteViewModel) }
                composable(Screen.Settings.route) { 
                    SettingsScreen(
                        initialName = userName,
                        initialSurname = userSurname,
                        initialEmail = userEmail,
                        initialAvatar = userAvatar,
                        currentTheme = currentTheme,
                        currentLanguage = currentLanguage,
                        onThemeChange = onThemeChange,
                        onLanguageChange = onLanguageChange,
                        onProfileUpdate = { newName, newSurname, newEmail, newBitmap ->
                            userName = newName
                            userSurname = newSurname
                            userEmail = newEmail
                            userAvatar = newBitmap
                        },
                        onSyncNow = { repository.scheduleSync() },
                        onLogout = { isAuthenticated = false }
                    )
                }
            }
        }
    }
}
