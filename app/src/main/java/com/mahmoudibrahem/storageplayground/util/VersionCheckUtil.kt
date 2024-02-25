package com.mahmoudibrahem.storageplayground.util

import android.os.Build

fun onVersionGTE33(action: () -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        action.invoke()
    }
}

fun onVersionGTE29(action: () -> Unit) {
    val version = Build.VERSION.SDK_INT
    if (version >= Build.VERSION_CODES.Q && version < Build.VERSION_CODES.TIRAMISU) {
        action.invoke()
    }
}

fun onVersionLT29(action: () -> Unit) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        action.invoke()
    }
}