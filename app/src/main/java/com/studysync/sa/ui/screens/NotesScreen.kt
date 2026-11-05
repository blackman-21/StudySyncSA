package com.studysync.sa.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
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
import com.studysync.sa.R
import com.studysync.sa.data.local.entity.NoteEntity
import com.studysync.sa.data.remote.BlobStorageManager
import com.studysync.sa.viewmodel.NoteViewModel
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(noteViewModel: NoteViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val blobStorageManager = remember { BlobStorageManager(context) }
    val notes by noteViewModel.notes.collectAsState()
    val searchQuery by noteViewModel.searchQuery.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedNoteForView by remember { mutableStateOf<NoteEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_note))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text(stringResource(R.string.study_notes), style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { noteViewModel.onSearchQueryChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.search_notes)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (notes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No study notes yet. Tap + to add one!", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notes) { note ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedNoteForView = note }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                if (!note.attachmentUrl.isNullOrEmpty()) {
                                    val bitmap = remember(note.attachmentUrl) {
                                        try {
                                            if (File(note.attachmentUrl).exists()) {
                                                BitmapFactory.decodeFile(note.attachmentUrl)
                                            } else null
                                        } catch (e: Exception) { null }
                                    }
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "Attachment",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(90.dp)
                                                .clip(RoundedCornerShape(6.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                    }
                                }

                                Text(note.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(note.body, style = MaterialTheme.typography.bodySmall, maxLines = 3)
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Note Dialog
    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var body by remember { mutableStateOf("") }
        var attachmentBitmap by remember { mutableStateOf<Bitmap?>(null) }
        var attachmentPath by remember { mutableStateOf<String?>(null) }

        val galleryLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                try {
                    context.contentResolver.openInputStream(it).use { inputStream ->
                        attachmentBitmap = BitmapFactory.decodeStream(inputStream)
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
                attachmentBitmap = it
            }
        }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(stringResource(R.string.add_note)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(stringResource(R.string.note_title)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = body,
                        onValueChange = { body = it },
                        label = { Text(stringResource(R.string.note_body)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Media Blob Attachment:", style = MaterialTheme.typography.labelMedium)

                    if (attachmentBitmap != null) {
                        Image(
                            bitmap = attachmentBitmap!!.asImageBitmap(),
                            contentDescription = "Attachment Preview",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Gallery Blob", style = MaterialTheme.typography.bodySmall)
                        }
                        OutlinedButton(
                            onClick = { cameraLauncher.launch() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Camera Blob", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (attachmentBitmap != null) {
                        val bytes = blobStorageManager.bitmapToBytes(attachmentBitmap!!)
                        val fileName = "blob_note_${System.currentTimeMillis()}.jpg"
                        attachmentPath = blobStorageManager.saveLocalBlob(fileName, bytes)
                        scope.launch {
                            blobStorageManager.uploadBlob("notes/$fileName", bytes)
                        }
                    }
                    noteViewModel.addNote(title, body, "module_1", attachmentPath)
                    showAddDialog = false
                }) {
                    Text("Save Note")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // View Note Details Dialog
    selectedNoteForView?.let { note ->
        AlertDialog(
            onDismissRequest = { selectedNoteForView = null },
            title = { Text(note.title, style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!note.attachmentUrl.isNullOrEmpty()) {
                        val bitmap = remember(note.attachmentUrl) {
                            try {
                                if (File(note.attachmentUrl).exists()) {
                                    BitmapFactory.decodeFile(note.attachmentUrl)
                                } else null
                            } catch (e: Exception) { null }
                        }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Full Attachment",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    Text(note.body, style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = {
                        noteViewModel.deleteNote(note)
                        selectedNoteForView = null
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                    Button(onClick = { selectedNoteForView = null }) {
                        Text("Close")
                    }
                }
            }
        )
    }
}
