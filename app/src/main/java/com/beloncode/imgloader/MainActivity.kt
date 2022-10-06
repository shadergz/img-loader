package com.beloncode.imgloader

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.beloncode.imgloader.databinding.ActivityMainBinding

import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    private val openIntentCallback = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {
        documentResult ->
        run {
            if (documentResult.resultCode == RESULT_OK) {
                val documentIntent = documentResult.data
                val documentFileURI = documentIntent?.data
                val documentFilePath = documentFileURI?.path

                loadImageDocument(documentFilePath.toString())
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private val requestIntent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)

    private lateinit var binding: ActivityMainBinding

    private val errorRuntimeARPRet = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        /* Checking whether own app has storage access permissions */
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                if (ContextCompat.checkSelfPermission(applicationContext,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                    val neededPermissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    ActivityCompat.requestPermissions(this, neededPermissions, 1)
                }
            } else {
                /* We don't have the permission yet, request for the user */
                if (!Environment.isExternalStorageManager()) {
                    startActivityIfNeeded(requestIntent, 1)
                }
            }
        } catch (error: Exception) {
            /* println(error.message) */
            Log.e(applicationContext.packageName, error.message.toString())
            exitProcess(errorRuntimeARPRet)
        }

    }

    private fun openDocumentViewer() {
        val openDocumentIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        openIntentCallback.launch(openDocumentIntent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.statusbar_menu, menu)
        return true
    }

    /* Control all 'option selected' events in the main status bar menu */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.loadImage -> {
                openDocumentViewer()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /* Function for handler the image file */
    private external fun loadImageDocument(uriPath: String)

    companion object {
        init {
            System.loadLibrary("image")
        }
    }
}

