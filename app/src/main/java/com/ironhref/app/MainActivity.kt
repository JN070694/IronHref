package com.ironhref.app

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: UrlDatabaseHelper
    private lateinit var adapter: UrlAdapter

    private val importLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { importFromFile(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = UrlDatabaseHelper(this)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val btnAdd = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.btnAdd)
        val btnImport = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.btnImport)
        val btnLaunchBrowser = findViewById<Button>(R.id.btnLaunchBrowser)

        adapter = UrlAdapter(
            dbHelper.getAllUrls().toMutableList(),
            onClickUrl = { entry -> openUrl(entry.url) },
            onLongPressUrl = { entry -> confirmDelete(entry) }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnAdd.setOnClickListener { showAddDialog() }
        btnImport.setOnClickListener { importLauncher.launch("*/*") }
        btnLaunchBrowser.setOnClickListener { launchBrowser() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(Menu.NONE, 1, Menu.NONE, "Delete All").apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            icon = getDrawable(android.R.drawable.ic_menu_delete)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            1 -> {
                confirmDeleteAll()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun confirmDeleteAll() {
        AlertDialog.Builder(this)
            .setTitle("Delete All URLs")
            .setMessage("This will permanently remove all saved URLs. Are you sure?")
            .setPositiveButton("Delete All") { _, _ ->
                dbHelper.deleteAllUrls()
                refreshList()
                Toast.makeText(this, "All URLs deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun launchBrowser() {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://"))
        val resolveInfo = packageManager.resolveActivity(browserIntent, 0)
        val browserPackage = resolveInfo?.activityInfo?.packageName

        if (browserPackage != null) {
            val launchIntent = packageManager.getLaunchIntentForPackage(browserPackage)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(launchIntent)
                return
            }
        }
        // Fallback if package resolution fails
        startActivity(Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_APP_BROWSER)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    private fun openUrl(url: String) {
        val fullUrl = if (url.startsWith("http://") || url.startsWith("https://")) {
            url
        } else {
            "https://$url"
        }
        val intent = Intent(Intent.ACTION_VIEW, fullUrl.toUri())
        startActivity(intent)
    }

    private fun showAddDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_url, null)
        val editTitle = dialogView.findViewById<EditText>(R.id.editTitle)
        val editUrl = dialogView.findViewById<EditText>(R.id.editUrl)

        AlertDialog.Builder(this)
            .setTitle("Add URL")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val title = editTitle.text.toString().trim()
                val url = editUrl.text.toString().trim()
                if (title.isNotEmpty() && url.isNotEmpty()) {
                    dbHelper.insertUrl(UrlEntry(title = title, url = url))
                    refreshList()
                } else {
                    Toast.makeText(this, "Title and URL are required", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmDelete(entry: UrlEntry) {
        AlertDialog.Builder(this)
            .setTitle("Delete")
            .setMessage("Remove \"${entry.title}\"?")
            .setPositiveButton("Delete") { _, _ ->
                dbHelper.deleteUrl(entry.id)
                refreshList()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun importFromFile(uri: Uri) {
        var imported = 0
        var skipped = 0
        try {
            contentResolver.openInputStream(uri)?.use { stream ->
                BufferedReader(InputStreamReader(stream)).forEachLine { line ->
                    val trimmed = line.trim()
                    if (trimmed.isEmpty()) return@forEachLine

                    if (trimmed.contains(",")) {
                        val parts = trimmed.split(",", limit = 2)
                        if (parts.size == 2 && parts[1].isNotBlank()) {
                            dbHelper.insertUrl(UrlEntry(title = parts[0].trim(), url = parts[1].trim()))
                            imported++
                        } else skipped++
                    } else if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                        dbHelper.insertUrl(UrlEntry(title = trimmed, url = trimmed))
                        imported++
                    } else {
                        skipped++
                    }
                }
            }
            refreshList()
            Toast.makeText(this, "Imported $imported URLs, skipped $skipped lines", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to read file: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun refreshList() {
        adapter.updateData(dbHelper.getAllUrls())
    }
}