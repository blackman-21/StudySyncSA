package com.studysync.sa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import androidx.room.Room
import com.studysync.sa.data.local.StudySyncDatabase
import com.studysync.sa.data.repository.StudySyncRepository
import com.studysync.sa.ui.navigation.Screen
import com.studysync.sa.ui.navigation.bottomNavItems
import com.studysync.sa.ui.screens.*
import com.studysync.sa.ui.theme.StudySyncSATheme
import com.studysync.sa.viewmodel.NoteViewModel
import com.studysync.sa.viewmodel.TaskViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val db = Room.databaseBuilder(
            applicationContext,
            StudySyncDatabase::class.java, "studysync_db"
        ).build()
        val repository = StudySyncRepository(applicationContext, db.studySyncDao())

        enableEdgeToEdge()
        setContent {
            // Theme selection state: "System", "Light", or "Dark"
            var appThemeSetting by remember { mutableStateOf("System") }
            val darkTheme = when (appThemeSetting) {
                "Light" -> false
                "Dark" -> true
                else -> isSystemInDarkTheme()
            }

            StudySyncSATheme(darkTheme = darkTheme) {
                MainApp(
                    repository = repository,
                    currentTheme = appThemeSetting,
                    onThemeChange = { appThemeSetting = it }
                )
            }
        }
    }
}

@Composable
fun MainApp(
    repository: StudySyncRepository,
    currentTheme: String,
    onThemeChange: (String) -> Unit
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

    var isAuthenticated by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("Student") }

    if (!isAuthenticated) {
        AuthScreen(onAuthSuccess = { name ->
            userName = name
            isAuthenticated = true
        })
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon!!, contentDescription = screen.title) },
                            label = { Text(screen.title) },
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
                        onThemeChange = onThemeChange,
                        onSyncNow = { repository.scheduleSync() },
                        onLogout = { isAuthenticated = false }
                    )
                }
            }
        }
    }
}
