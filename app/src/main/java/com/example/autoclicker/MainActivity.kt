package com.example.autoclicker

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnAccessibility = findViewById<Button>(R.id.btnAccessibility)
        val btnOverlayPermission = findViewById<Button>(R.id.btnOverlayPermission)
        val btnStartOverlay = findViewById<Button>(R.id.btnStartOverlay)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)

        btnAccessibility.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        btnOverlayPermission.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            } else {
                tvStatus.text = "Ekran üzeri gösterme izni zaten verilmiş."
            }
        }

        btnStartOverlay.setOnClickListener {
            val overlayOk = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)
            val accessibilityOk = isAccessibilityServiceEnabled()

            if (!overlayOk) {
                tvStatus.text = "Önce 'ekranın üzerinde gösterme' iznini vermelisin."
                return@setOnClickListener
            }
            if (!accessibilityOk) {
                tvStatus.text = "Önce Erişilebilirlik iznini vermelisin."
                return@setOnClickListener
            }

            startService(Intent(this, OverlayService::class.java))
            tvStatus.text = "Kontrol paneli açıldı, ekranın üzerinde göreceksin."
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expectedComponentName = "$packageName/${ClickAccessibilityService::class.java.name}"
        val enabledServicesSetting = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val splitter = TextUtils.SimpleStringSplitter(':')
        splitter.setString(enabledServicesSetting)
        while (splitter.hasNext()) {
            if (splitter.next().equals(expectedComponentName, ignoreCase = true)) {
                return true
            }
        }
        return false
    }
}
