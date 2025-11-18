package com.github.mheerwaarden.eventdemo.ui.components

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.extension
import kotlinx.coroutines.launch

/**
 * A button that launches the file picker dialog. It only allows selecting photos.
 * Note that FileKit does not support selecting directories.
 */
@Composable
fun FileKitPhotoUploadScreen(onSelectFile: (PlatformFile) -> Unit) {
    val coroutineScope = rememberCoroutineScope()

    // 1. Set up the file picker launcher from the vinceglb/filekit library
    val filePicker = rememberFilePickerLauncher { platformFile ->
        // This lambda is called when the user successfully selects a file.
        // It provides a multiplatform PlatformFile object.
        if (platformFile != null) {
            if (isPhotoFile(platformFile)) {
                coroutineScope.launch {
                    onSelectFile(platformFile)
                }
            } else {
                println("No photo file selected.")
            }
        } else {
            // User cancelled the picker
            println("File selection cancelled.")
        }
    }

    // 2. A button in your UI to trigger the file picker
    Button(onClick = { filePicker.launch() }) {
        Text("Select File")
    }
}

/**
 * A helper function to determine if a file is a photo based on its extension.
 */
private fun isPhotoFile(file: PlatformFile): Boolean {
    val validExtensions = setOf("jpg", "jpeg", "png", "heic", "heif", "webp")
    return file.extension.lowercase() in validExtensions
}
