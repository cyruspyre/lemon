package com.cyruspyre.lemon

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.Environment
import android.os.storage.StorageManager
import android.provider.Settings
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.cyruspyre.lemon.databinding.ActivityMainBinding
import com.cyruspyre.lemon.databinding.PermReqBinding
import com.cyruspyre.lemon.entity.EntityView
import com.cyruspyre.lemon.tabs.onTabChange
import java.nio.file.Path

lateinit var changePath: (Path?) -> Unit
lateinit var goBack: () -> Unit
lateinit var active: () -> EntityView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Environment.isExternalStorageManager()) return start()

        val binding = PermReqBinding.inflate(layoutInflater)
        val launcher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (Environment.isExternalStorageManager()) return@registerForActivityResult start()

            Toast.makeText(this, "Permission was not granted", Toast.LENGTH_SHORT).show()
        }

        binding.button.setOnClickListener {
            val intent = Intent(
                Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                "package:$packageName".toUri()
            )

            launcher.launch(intent)
        }

        setContentView(binding.root)
    }

    fun start() {
        VOLUMES = getSystemService(StorageManager::class.java).storageVolumes
        DISPLAY_METRICS = resources.displayMetrics

        val binding = ActivityMainBinding.inflate(layoutInflater)
        val root = binding.root
        val tabs = binding.tabs
        val changePathBar = navBar(this, binding.navBar)

        active = { tabs.active.active }
        changePath = tmp@{
            val view = tabs.active.active
            val stack = view.stack
            val size = stack.size

            if (view.idx < size - 1) stack.subList(++view.idx, size).clear()
            else view.idx = size

            stack.add(it)
            view.update()
        }
        goBack = {
            val tmp = tabs.active.active

            if (tmp.idx > 0) {
                tmp.idx--
                tmp.update()
            }
        }
        onPathChange = {
            changePathBar(it)
            tabs.adapter.notifyItemChanged(tabs.adapter.active, Unit)
        }
        onTabChange = {
            if (root.childCount > 2) root.removeViewAt(2)

            root.addView(it)
            onPathChange(it.active.path)
        }

        setContentView(binding.root)
        tabs.addTab()

        onBackPressedDispatcher.addCallback { goBack() }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val view = currentFocus
        val cond = ev.action != MotionEvent.ACTION_UP || view !is EditText

        if (cond) return super.dispatchTouchEvent(ev)

        val rect = Rect()

        view.getGlobalVisibleRect(rect)

        if (!rect.contains(ev.x.toInt(), ev.y.toInt())) {
            view.clearFocus()
            getSystemService(InputMethodManager::class.java).hideSoftInputFromWindow(
                view.windowToken, 0
            )
        }

        return super.dispatchTouchEvent(ev)
    }
}