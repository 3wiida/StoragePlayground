package com.mahmoudibrahem.storageplayground.model

import android.net.Uri

data class ExternalImage(
    val id: Long,
    val name: String,
    val uri: Uri
)
