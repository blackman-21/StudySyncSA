package com.studysync.sa

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import androidx.room.Room
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
        val prefs = newBase.getSharedPreferences("study_sync_prefs", Context.MODE_PRIVATE)
        val lang = prefs.getString("app_language", "English") ?: "English"
        val langCode = LocaleHelper.getLanguageCode(lang)
        val localizedContext = LocaleHelper.applyLocale(newBase, langCode)
        super.attachBaseContext(localizedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(applicationContext)

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
    var userName by remember { mutableStateOf("Student") }

    if (showWelcomeScreen) {
        WelcomeScreen(onGetStarted = { showWelcomeScreen = false })
    } else if (!isAuthenticated) {
        AuthScreen(onAuthSuccess = { name ->
            userName = name
            isAuthenticated = true
        })
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
                        currentTheme = currentTheme,
                        currentLanguage = currentLanguage,
                        onThemeChange = onThemeChange,
                        onLanguageChange = onLanguageChange,
                        onSyncNow = { repository.scheduleSync() },
                        onLogout = { isAuthenticated = false }
                    )
                }
            }
        }
    }
}
