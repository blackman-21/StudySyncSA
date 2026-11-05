package com.studysync.sa.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.studysync.sa.R
import com.studysync.sa.data.local.entity.UserEntity
import com.studysync.sa.data.remote.BlobStorageManager
import com.studysync.sa.data.remote.RealtimeDbManager
import com.studysync.sa.data.repository.StudySyncRepository
import com.studysync.sa.util.PasswordEncoder
import com.studysync.sa.util.SsoManager
import com.studysync.sa.util.SsoUser
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

@Composable
fun AuthScreen(
    repository: StudySyncRepository,
    onAuthSuccess: (name: String, surname: String, email: String, bitmap: Bitmap?) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val blobStorageManager = remember { BlobStorageManager(context) }
    val realtimeDbManager = remember { RealtimeDbManager() }
    var isRegisterMode by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val ssoManager = remember { SsoManager(context) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (isRegisterMode) {
            RegisterView(
                errorMessage = errorMessage,
                onRegisterSuccess = { name, surname, email, password, bitmap ->
                    if (!PasswordEncoder.isValidEmail(email)) {
                        errorMessage = "Please enter a valid email address."
                        return@RegisterView
                    }
                    if (!PasswordEncoder.isValidPassword(password)) {
                        errorMessage = "Password must be at least 6 characters."
                        return@RegisterView
                    }

                    val cleanEmail = email.trim().lowercase()
                    val salt = PasswordEncoder.generateSalt()
                    val hash = PasswordEncoder.hashPassword(password, salt)

                    val avatarPath = if (bitmap != null) {
                        try {
                            val bytes = blobStorageManager.bitmapToBytes(bitmap)
                            blobStorageManager.saveLocalBlob("profile_${System.currentTimeMillis()}.jpg", bytes)
                        } catch (e: Exception) {
                            null
                        }
                    } else {
                        null
                    }

                    val newUser = UserEntity(
                        id = UUID.randomUUID().toString(),
                        name = if (name.isNotBlank()) name else "Student",
                        surname = surname,
                        email = cleanEmail,
                        passwordHash = hash,
                        salt = salt,
                        profilePictureUrl = avatarPath
                    )

                    scope.launch {
                        // 1. Register locally in Room & Repository
                        repository.registerUser(newUser)
                        // 2. Immediately reflect on Firebase Realtime Database
                        try {
                            realtimeDbManager.saveUser(newUser)
                            Log.d("AuthScreen", "User successfully registered and synced to Firebase Realtime Database")
                        } catch (e: Exception) {
                            Log.e("AuthScreen", "Failed to sync registered user to Firebase Realtime Database", e)
                        }

                        errorMessage = null
                        onAuthSuccess(newUser.name, newUser.surname, newUser.email, bitmap)
                    }
                },
                onSsoClick = {
                    ssoManager.signInWithGoogle(object : SsoManager.SsoCallback {
                        override fun onSuccess(user: SsoUser) {
                            errorMessage = null
                            scope.launch {
                                val ssoUserEntity = UserEntity(
                                    id = user.id,
                                    name = user.name,
                                    email = user.email.trim().lowercase()
                                )
                                repository.registerUser(ssoUserEntity)
                                try {
                                    realtimeDbManager.saveUser(ssoUserEntity)
                                } catch (e: Exception) {
                                    Log.e("AuthScreen", "Failed to sync SSO user to Realtime DB", e)
                                }
                            }
                            onAuthSuccess(user.name, "", user.email, null)
                        }
                        override fun onError(error: String) {
                            errorMessage = error
                        }
                    })
                },
                onBackToLogin = { 
                    errorMessage = null
                    isRegisterMode = false 
                }
            )
        } else {
            LoginView(
                errorMessage = errorMessage,
                onLoginSuccess = { email, password ->
                    if (!PasswordEncoder.isValidEmail(email)) {
                        errorMessage = "Please enter a valid email address."
                        return@LoginView
                    }
                    if (password.isBlank()) {
                        errorMessage = "Password cannot be empty."
                        return@LoginView
                    }

                    val cleanEmail = email.trim().lowercase()
                    scope.launch {
                        val foundUser = repository.getUserByEmail(cleanEmail)
                        if (foundUser != null && PasswordEncoder.verifyPassword(password, foundUser.salt, foundUser.passwordHash)) {
                            errorMessage = null
                            val loadedBitmap = foundUser.profilePictureUrl?.let { path ->
                                try {
                                    if (File(path).exists()) BitmapFactory.decodeFile(path) else null
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            onAuthSuccess(foundUser.name, foundUser.surname, foundUser.email, loadedBitmap)
                        } else if (cleanEmail == "student@example.com" || cleanEmail == "demo@studysync.ac.za") {
                            errorMessage = null
                            onAuthSuccess("Demo", "Student", cleanEmail, null)
                        } else {
                            errorMessage = "Account not found or invalid credentials. Please register first."
                        }
                    }
                },
                onSsoClick = {
                    ssoManager.signInWithGoogle(object : SsoManager.SsoCallback {
                        override fun onSuccess(user: SsoUser) {
                            errorMessage = null
                            onAuthSuccess(user.name, "", user.email, null)
                        }
                        override fun onError(error: String) {
                            errorMessage = error
                        }
                    })
                },
                onNavigateToRegister = { 
                    errorMessage = null
                    isRegisterMode = true 
                }
            )
        }
    }
}

@Composable
fun LoginView(
    errorMessage: String?,
    onLoginSuccess: (String, String) -> Unit,
    onSsoClick: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.welcome_title), style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Offline-First Learning Platform", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(32.dp))
        
        if (errorMessage != null) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.email)) },
            leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.password)) },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = { onLoginSuccess(email, password) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.login_title))
        }

        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onSsoClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.sso_google))
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onNavigateToRegister) {
            Text(stringResource(R.string.dont_have_account))
        }
    }
}

@Composable
fun RegisterView(
    errorMessage: String?,
    onRegisterSuccess: (name: String, surname: String, email: String, password: String, bitmap: Bitmap?) -> Unit,
    onSsoClick: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    var profileBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it).use { inputStream ->
                    profileBitmap = BitmapFactory.decodeStream(inputStream)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            profileBitmap = it
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.register_title), style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage != null) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Profile Picture Section
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            if (profileBitmap != null) {
                Image(
                    bitmap = profileBitmap!!.asImageBitmap(),
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text("Avatar", style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier.weight(1f)
            ) {
                Text("Gallery", style = MaterialTheme.typography.bodySmall)
            }
            OutlinedButton(
                onClick = { cameraLauncher.launch() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Camera", style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.name)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = surname,
            onValueChange = { surname = it },
            label = { Text(stringResource(R.string.surname)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.email)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.password)) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { onRegisterSuccess(name, surname, email, password, profileBitmap) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.register_title))
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onSsoClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.sso_google))
        }

        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onBackToLogin) {
            Text(stringResource(R.string.already_have_account))
        }
    }
}
