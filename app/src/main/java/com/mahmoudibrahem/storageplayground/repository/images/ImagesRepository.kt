package com.mahmoudibrahem.storageplayground.repository.images

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.mahmoudibrahem.storageplayground.model.ExternalImage
import com.mahmoudibrahem.storageplayground.model.InternalImage

interface ImagesRepository {

    suspend fun loadImagesFromInternalStorage(context: Context): List<InternalImage>

    suspend fun loadImagesFromExternalStorage(context: Context): List<ExternalImage>

    suspend fun saveImageIntoInternalStorage(
        context: Context,
        bitmap: Bitmap,
        name: String
    ): Boolean

    suspend fun saveImageIntoExternalStorage(
        context: Context,
        bitmap: Bitmap,
        name: String
    ): Boolean

    suspend fun deleteImageFromInternalStorage(context: Context, imageName: String): Boolean

    suspend fun deleteImageFromExternalStorage(
        context: Context,
        uri: Uri,
        intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest>,
        onImageDeleted: () -> Unit
    )

}