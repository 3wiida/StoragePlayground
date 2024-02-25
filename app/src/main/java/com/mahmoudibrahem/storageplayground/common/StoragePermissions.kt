package com.mahmoudibrahem.storageplayground.common

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi

sealed class StoragePermissions(val permission: String) {
     object ReadExternalStorage : StoragePermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
     object WriteExternalStorage : StoragePermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
     object ReadMediaImages : StoragePermissions(Manifest.permission.READ_MEDIA_IMAGES)

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
     object ReadMediaVideos : StoragePermissions(Manifest.permission.READ_MEDIA_VIDEO)

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
     object ReadMediaAudios : StoragePermissions(Manifest.permission.READ_MEDIA_AUDIO)
}