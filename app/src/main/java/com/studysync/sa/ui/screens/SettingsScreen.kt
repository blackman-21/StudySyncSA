package com.studysync.sa.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.studysync.sa.R
import com.studysync.sa.data.remote.BlobStorageManager
import com.studysync.sa.notification.LocalNotificationManager
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    initialName: String,
    initialSurname: String,
    initialEmail: String,
    initialAvatar: Bitmap?,
    currentTheme: String,
    currentLanguage: String,
    onThemeChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onProfileUpdate: (String, String, String, Bitmap?) -> Unit,
    onSyncNow: () -> Unit,
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val blobStorageManager = remember { BlobStorageManager(context) }
    val notificationManager = remember { LocalNotificationManager(context) }

    var notificationsEnabled by remember { mutableStateOf(true) }
    val languages = listOf("English", "isiZulu", "Afrikaans", "isiXhosa")
    var langExpanded by remember { mutableStateOf(false) }
    var themeExpanded by remember { mutableStateOf(false) }
    val themes = listOf("System", "Light", "Dark")

    // Profile States initialized from registered user information
    var isEditingProfile by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(initialName) }
    var surname by remember { mutableStateOf(initialSurname) }
    var email by remember { mutableStateOf(initialEmail) }
    var profileBitmap by remember { mutableStateOf<Bitmap?>(initialAvatar) }

    // Launchers for image capture/selection
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it).use { inputStream ->
                    val bmp = BitmapFactory.decodeStream(inputStream)
                    profileBitmap = bmp
                    if (bmp != null) {
                        val bytes = blobStorageManager.bitmapToBytes(bmp)
                        blobStorageManager.saveLocalBlob("profile_avatar.jpg", bytes)
                        scope.launch {
                            blobStorageManager.uploadBlob("avatars/user_avatar.jpg", bytes)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let { bmp ->
            profileBitmap = bmp
            val bytes = blobStorageManager.bitmapToBytes(bmp)
            blobStorageManager.saveLocalBlob("profile_avatar.jpg", bytes)
            scope.launch {
                blobStorageManager.uploadBlob("avatars/user_avatar.jpg", bytes)
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                cameraLauncher.launch()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(stringResource(R.string.nav_settings), style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        // Profile Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.profile), style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
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
                            Text("Avatar", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                        }
                    }

                    if (!isEditingProfile) {
                        Column {
                            Text("$name $surname".trim().ifEmpty { "Student" }, style = MaterialTheme.typography.bodyLarge)
                            Text(email.ifEmpty { "student@example.com" }, style = MaterialTheme.typography.bodySmall)
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedButton(onClick = { galleryLauncher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                                Text("Gallery Blob", style = MaterialTheme.typography.bodySmall)
                            }
                            OutlinedButton(
                                onClick = {
                                    val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                                    if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                        try {
                                            cameraLauncher.launch()
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    } else {
                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Camera Blob", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isEditingProfile) {
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
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            isEditingProfile = false
                            onProfileUpdate(name, surname, email, profileBitmap)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Changes")
                    }
                } else {
                    OutlinedButton(
                        onClick = { isEditingProfile = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Edit Profile")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        // App Theme Selector
        Text(stringResource(R.string.appearance), style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Box {
            OutlinedButton(onClick = { themeExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text("${stringResource(R.string.theme)}: $currentTheme")
            }
            DropdownMenu(expanded = themeExpanded, onDismissRequest = { themeExpanded = false }) {
                themes.forEach { theme ->
                    DropdownMenuItem(
                        text = { Text(theme) },
                        onClick = {
                            onThemeChange(theme)
                            themeExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.enable_notifications))
            Switch(checked = notificationsEnabled, onCheckedChange = { notificationsEnabled = it })
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box {
            OutlinedButton(onClick = { langExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text("${stringResource(R.string.language)}: $currentLanguage")
            }
            DropdownMenu(expanded = langExpanded, onDismissRequest = { langExpanded = false }) {
                languages.forEach { lang ->
                    DropdownMenuItem(
                        text = { Text(lang) },
                        onClick = {
                            onLanguageChange(lang)
                            langExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                onSyncNow()
                notificationManager.showNotification(
                    notificationId = 202,
                    title = "Sync Initiated",
                    message = "Background synchronization with REST API & NoSQL database started.",
                    channelId = LocalNotificationManager.CHANNEL_ID_SYNC
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.sync_now))
        }

        Spacer(modifier = Modifier.height(32.dp))

        TextButton(
            onClick = onLogout,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Text(stringResource(R.string.log_out))
        }
    }
}
