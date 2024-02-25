package com.mahmoudibrahem.storageplayground.ui.main

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.mahmoudibrahem.storageplayground.R
import com.mahmoudibrahem.storageplayground.common.StoragePermissions
import com.mahmoudibrahem.storageplayground.databinding.ActivityMainBinding
import com.mahmoudibrahem.storageplayground.util.onVersionGTE29
import com.mahmoudibrahem.storageplayground.util.onVersionGTE33
import com.mahmoudibrahem.storageplayground.util.onVersionLT29
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    var onImageTaken: ((Bitmap?) -> Unit)? = null
    var onExternalImageDeleted: ((Boolean) -> Unit)? = null
    lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    lateinit var takePhotoLauncher: ActivityResultLauncher<Void?>
    lateinit var intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavBar.setupWithNavController(navController)
        permissionLauncher = requestRequiredPermissions()
        permissionLauncher.launch(getRequiredPermissions().toTypedArray())
        takePhotoLauncher = registerTakePhotoLauncher()
        intentSenderLauncher = registerDeleteIntentSender()
    }


    /*
    * These permissions only required for shared storage.
    * Internal storage doesn't require any permissions
    * */
    private fun getRequiredPermissions(): List<String> {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.addAll(
                listOf(
                    StoragePermissions.ReadMediaImages.permission,
                    StoragePermissions.ReadMediaAudios.permission,
                    StoragePermissions.ReadMediaVideos.permission
                )
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(
                StoragePermissions.ReadExternalStorage.permission
            )
        } else {
            permissions.addAll(
                listOf(
                    StoragePermissions.ReadExternalStorage.permission,
                    StoragePermissions.WriteExternalStorage.permission
                )
            )
        }
        return permissions
    }

    private fun requestRequiredPermissions(): ActivityResultLauncher<Array<String>> {
        return registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { results ->
            onVersionGTE33 {
                if (results[StoragePermissions.ReadMediaImages.permission] == false) {
                    handleDeniedPermission(StoragePermissions.ReadMediaImages.permission)
                }
                if (results[StoragePermissions.ReadMediaAudios.permission] == false) {
                    handleDeniedPermission(StoragePermissions.ReadMediaAudios.permission)
                }
                if (results[StoragePermissions.ReadMediaVideos.permission] == false) {
                    handleDeniedPermission(StoragePermissions.ReadMediaVideos.permission)
                }
            }

            onVersionGTE29 {
                if (results[StoragePermissions.ReadExternalStorage.permission] == false) {
                    handleDeniedPermission(StoragePermissions.ReadExternalStorage.permission)
                }
            }

            onVersionLT29 {
                if (results[StoragePermissions.ReadExternalStorage.permission] == false) {
                    handleDeniedPermission(StoragePermissions.ReadExternalStorage.permission)
                }
                if (results[StoragePermissions.WriteExternalStorage.permission] == false) {
                    handleDeniedPermission(StoragePermissions.WriteExternalStorage.permission)
                }
            }
        }
    }

    private fun handleDeniedPermission(permission: String) {
        if (shouldShowRequestPermissionRationale(permission)) {
            AlertDialog.Builder(this).apply {
                setTitle(R.string.rational_title)
                setMessage(R.string.rational_message)
                setPositiveButton("Ok") { dialog, _ ->
                    dialog.dismiss()
                    permissionLauncher.launch(getRequiredPermissions().toTypedArray())
                }
            }.show()
        } else {
            AlertDialog.Builder(this).apply {
                setTitle(R.string.settings_dialog_title)
                setMessage(R.string.settings_dialog_message)
                setPositiveButton("Open Settings") { dialog, _ ->
                    dialog.dismiss()
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.setData(uri)
                    startActivity(intent)
                }
            }.show()
        }
    }

    private fun registerTakePhotoLauncher(): ActivityResultLauncher<Void?> {
        return registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { image ->
            onImageTaken?.invoke(image)
        }
    }

    private fun registerDeleteIntentSender(): ActivityResultLauncher<IntentSenderRequest> {
        return registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            if (it.resultCode == RESULT_OK) {
                onExternalImageDeleted?.invoke(true)
                Toast.makeText(this, "Deleted Successfully", Toast.LENGTH_SHORT).show()
            } else {
                onExternalImageDeleted?.invoke(false)
                Toast.makeText(this, "Can't Delete Image", Toast.LENGTH_SHORT).show()
            }
        }
    }

}