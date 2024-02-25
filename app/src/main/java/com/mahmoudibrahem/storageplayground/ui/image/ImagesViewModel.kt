package com.mahmoudibrahem.storageplayground.ui.image

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahmoudibrahem.storageplayground.model.ExternalImage
import com.mahmoudibrahem.storageplayground.model.InternalImage
import com.mahmoudibrahem.storageplayground.repository.images.ImagesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImagesViewModel @Inject constructor(
    private val imagesRepository: ImagesRepository
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab = _selectedTab.asStateFlow()

    private val _externalImages = MutableStateFlow(listOf<ExternalImage>())
    val externalImages = _externalImages.asStateFlow()

    private val _internalImages = MutableStateFlow(listOf<InternalImage>())
    val internalImage = _internalImages.asStateFlow()

    private val _isPhotoSaved = MutableStateFlow(false)
    val isPhotoSaved = _isPhotoSaved.asStateFlow()

    private val _isInternalImageDeleted = MutableStateFlow(false)
    val isInternalImageDeleted = _isInternalImageDeleted.asStateFlow()

    fun onTabChanged(tabPosition: Int) {
        _selectedTab.update { tabPosition }
    }

    fun checkReadPermission(context: Context): Boolean {
        val permission = getReadPermission()
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun getReadPermission(): String {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        return permission
    }

    fun loadImagesFromExternalStorage(context: Context) {
        viewModelScope.launch {
            val photos = imagesRepository.loadImagesFromExternalStorage(context)
            _externalImages.update { photos }
        }
    }

    fun loadImagesFromInternalStorage(context: Context) {
        viewModelScope.launch {
            val photos = imagesRepository.loadImagesFromInternalStorage(context)
            _internalImages.update { photos }
        }
    }

    fun saveImage(
        context: Context,
        image: Bitmap,
        name: String
    ) {
        viewModelScope.launch {
            when (selectedTab.value) {
                0 -> {
                    val isSaved = imagesRepository.saveImageIntoInternalStorage(
                        context,
                        image,
                        name
                    )
                    _isPhotoSaved.update { isSaved }
                }

                1 -> {
                    val isSaved = imagesRepository.saveImageIntoExternalStorage(
                        context,
                        image,
                        name
                    )
                    _isPhotoSaved.update { isSaved }
                }
            }
        }
    }

    fun deleteImageFromInternalStorage(context: Context, imageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val isDeleted = imagesRepository.deleteImageFromInternalStorage(context, imageName)
            _isInternalImageDeleted.update { isDeleted }
            delay(500)
            _isInternalImageDeleted.update { false }
        }
    }

    fun deleteImageFromExternalStorage(
        context: Context,
        uri: Uri,
        intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            imagesRepository.deleteImageFromExternalStorage(
                context,
                uri,
                intentSenderLauncher
            ) { loadImagesFromExternalStorage(context) }
        }
    }

}