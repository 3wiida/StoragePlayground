package com.mahmoudibrahem.storageplayground.repository.images

import android.annotation.SuppressLint
import android.app.Activity
import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.mahmoudibrahem.storageplayground.model.ExternalImage
import com.mahmoudibrahem.storageplayground.model.InternalImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID

class ImagesRepositoryImpl : ImagesRepository {

    @SuppressLint("FileEndsWithExt")
    override suspend fun loadImagesFromInternalStorage(context: Context): List<InternalImage> {
        return withContext(Dispatchers.IO) {
            val files = context.filesDir.listFiles()
            files?.filter { it.canRead() && it.isFile && it.name.endsWith(".png") }?.map {
                val bytes = it.readBytes()
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                InternalImage(name = it.name, bitmap)
            } ?: listOf()
        }
    }

    override suspend fun loadImagesFromExternalStorage(context: Context): List<ExternalImage> {
        return withContext(Dispatchers.IO) {
            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else MediaStore.Images.Media.EXTERNAL_CONTENT_URI

            val projection = listOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
            )

            val photos = mutableListOf<ExternalImage>()

            context.contentResolver.query(
                collection,
                projection.toTypedArray(),
                null,
                null,
                "${MediaStore.Images.Media.DISPLAY_NAME} ASC"
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)
                    val uri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id
                    )

                    val externalImage = ExternalImage(id = id, name = name, uri = uri)
                    photos.add(externalImage)
                }
                photos
            } ?: emptyList()
        }
    }


    override suspend fun saveImageIntoInternalStorage(
        context: Context,
        bitmap: Bitmap,
        name: String
    ): Boolean {
        return try {
            context.openFileOutput(name, Context.MODE_PRIVATE).use { stream ->
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)) {
                    throw IOException("Couldn't save the image")
                }
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun saveImageIntoExternalStorage(
        context: Context,
        bitmap: Bitmap,
        name: String
    ): Boolean {
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, "images/png")
        }

        return try {
            context.contentResolver.insert(collection, contentValues)?.also { uri ->
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)) {
                        throw IOException("Couldn't save the image")
                    }
                }
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun deleteImageFromInternalStorage(
        context: Context,
        imageName: String
    ): Boolean {
        return try {
            context.deleteFile(imageName)
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun deleteImageFromExternalStorage(
        context: Context,
        uri: Uri,
        intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest>,
        onImageDeleted: () -> Unit
    ) {
        try {
            context.contentResolver.delete(uri, null, null)
            onImageDeleted.invoke()
        } catch (e: SecurityException) {
            val intentSender = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                MediaStore.createDeleteRequest(context.contentResolver, listOf(uri)).intentSender
            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                val recoverableSecurityException = e as? RecoverableSecurityException
                recoverableSecurityException?.userAction?.actionIntent?.intentSender
            } else null

            intentSender?.let { sender ->
                intentSenderLauncher.launch(
                    IntentSenderRequest.Builder(sender).build()
                )
            }
        }
    }
}