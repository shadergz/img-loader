package com.tool.imgloader

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
import android.widget.SearchView.OnQueryTextListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.tool.imgloader.databinding.ActivityMainBinding
import java.util.Vector

class MainActivity : AppCompatActivity() {

    private val openIntentCallback = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {
        documentResult ->
        run {
            if (documentResult.resultCode != RESULT_OK)
                return@registerForActivityResult

        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private val requestIntent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
    private lateinit var binding: ActivityMainBinding

    private val selectedItems = Vector<ItemModel>()
    private val dffItems = ArrayList<ItemModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.dffItems.apply {

            dffItems.add(ItemModel("cj_model0.dff", 0, 0))
            dffItems.add(ItemModel("cj_mouth.dff", 0, 0))
            dffItems.add(ItemModel("cj_feet.dff", 0, 0))

            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(false)
            adapter = AdapterItems(dffItems) { dffModel,selected ->
                when (selected) {
                    OnSelectedType.SELECTED -> selectedItems.add(dffModel)
                    OnSelectedType.UNSELECTED-> selectedItems.remove(dffModel)
                    OnSelectedType.PRESSED-> {}
                }
            }
        }

        binding.searchModels.apply {
            clearFocus()
            setOnQueryTextListener(object: OnQueryTextListener {
                override fun onQueryTextChange(newText: String?): Boolean {
                    return false
                }

                override fun onQueryTextSubmit(query: String?): Boolean {
                    (binding.dffItems.adapter as AdapterItems).apply {
                        filterList(query!!)
                    }
                    return true
                }
            })
            setOnCloseListener {
                (binding.dffItems.adapter as AdapterItems).apply {
                    removeFilter()
                }
                true
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Checking if our app has the storage access permissions
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
                // We don't have the permissions yet, requesting to the user
                if (!Environment.isExternalStorageManager()) {
                    startActivityIfNeeded(requestIntent, 1)
                }
            }
        } catch (error: Exception) {
            Log.e(applicationContext.packageName, error.message.toString())
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.loadImage -> {
                openDocumentViewer()
                return true
            }
            R.id.selectAll -> {
                (binding.dffItems.adapter as AdapterItems).apply {
                    selectAll()
                }
                return true
            }
            R.id.unselectAll -> {
                (binding.dffItems.adapter as AdapterItems).apply {
                    unselectAll()
                }
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}

