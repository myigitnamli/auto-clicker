package com.example.autoclicker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.app.NotificationCompat

/**
 * Ekranın üzerinde kalıcı bir kontrol paneli gösteren servis.
 * Kullanıcı: tıklama aralığını girer, dokunulacak bölgeyi ekrandan sürükleyerek
 * seçer, ardından Başlat'a basarak seçilen bölge içinde rastgele noktalara
 * belirlenen aralıkla otomatik dokunma başlatır.
 */
class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var controlPanelView: View? = null
    private var regionSelectView: RegionSelectView? = null

    private var selectedRegion: Rect? = null
    private var intervalMs: Long = 500
    private var isClicking = false
    private var keywordList: List<String> = emptyList()
    private var minMatches: Int = 1

    private val clickHandler = Handler(Looper.getMainLooper())
    private val clickRunnable = object : Runnable {
        override fun run() {
            if (!isClicking) return
            selectedRegion?.let { rect ->
                // Bölgedeki anahtar kelimelerden en az minMatches tanesi
                // aynı anda görünüyorsa, o bölgedeki uygun tıklanabilir
                // butona gerçek bir tıklama gönderir.
                ClickAccessibilityService.instance?.performKeywordClick(rect, keywordList, minMatches)
            }
            clickHandler.postDelayed(this, intervalMs)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        startForegroundWithNotification()
        showControlPanel()
    }

    private fun startForegroundWithNotification() {
        val channelId = "auto_clicker_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "MY Günlük", NotificationManager.IMPORTANCE_LOW
            )
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("MY Günlük çalışıyor")
            .setContentText("Kontrol paneli ekranda açık")
            .setSmallIcon(android.R.drawable.ic_menu_myplaces)
            .setOngoing(true)
            .build()
        startForeground(1, notification)
    }

    private fun windowType(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

    private fun showControlPanel() {
        if (controlPanelView != null) return

        val inflater = LayoutInflater.from(this)
        val panel = inflater.inflate(R.layout.overlay_control_panel, null)
        controlPanelView = panel

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            windowType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 20
        params.y = 100

        // Paneli sürükleyerek taşıma
        val dragHandle = panel.findViewById<TextView>(R.id.tvDrag)
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        dragHandle.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(panel, params)
                    true
                }
                else -> false
            }
        }

        val etInterval = panel.findViewById<EditText>(R.id.etInterval)
        val etKeywords = panel.findViewById<EditText>(R.id.etKeywords)
        val etMinMatches = panel.findViewById<EditText>(R.id.etMinMatches)
        val tvRegionInfo = panel.findViewById<TextView>(R.id.tvRegionInfo)
        val btnSelectRegion = panel.findViewById<Button>(R.id.btnSelectRegion)
        val btnToggleStart = panel.findViewById<Button>(R.id.btnToggleStart)
        val btnClosePanel = panel.findViewById<Button>(R.id.btnClosePanel)

        // Panel varsayılan olarak FLAG_NOT_FOCUSABLE ile açılıyor ki arkadaki
        // uygulamayı/oyunu engellemesin. Ama bu bayrak açıkken klavye asla
        // açılmaz. Bu yüzden bir EditText'e dokunulduğunda bayrağı geçici
        // olarak kaldırıp klavyeyi açıyoruz; yazma bitince (focus kaybolunca)
        // bayrağı geri takıp arkadaki uygulamaya dokunuşların geçmesine
        // tekrar izin veriyoruz.
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        fun enableTextInput(target: EditText) {
            params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
            windowManager.updateViewLayout(panel, params)
            target.requestFocus()
            imm.showSoftInput(target, InputMethodManager.SHOW_IMPLICIT)
        }

        fun disableTextInput(target: EditText) {
            imm.hideSoftInputFromWindow(target.windowToken, 0)
            params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            windowManager.updateViewLayout(panel, params)
        }

        listOf(etInterval, etKeywords, etMinMatches).forEach { editText ->
            editText.setOnClickListener { enableTextInput(editText) }
            editText.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) disableTextInput(editText)
            }
        }

        btnSelectRegion.setOnClickListener {
            etInterval.clearFocus()
            etKeywords.clearFocus()
            etMinMatches.clearFocus()
            imm.hideSoftInputFromWindow(panel.windowToken, 0)
            params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            windowManager.updateViewLayout(panel, params)
            startRegionSelection(tvRegionInfo)
        }

        btnToggleStart.setOnClickListener {
            // Anahtar kelime/aralık kutusunda klavye hâlâ açıksa ve panel
            // odaklanabilir durumdaysa, erişilebilirlik servisi ekranı
            // tararken kendi panelimizi (klavye açık EditText'i) bulup ona
            // tıklayabilir. Bunu önlemek için başlamadan önce klavyeyi ve
            // odağı kesin olarak kapatıyoruz.
            etInterval.clearFocus()
            etKeywords.clearFocus()
            etMinMatches.clearFocus()
            imm.hideSoftInputFromWindow(panel.windowToken, 0)
            params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            windowManager.updateViewLayout(panel, params)

            if (ClickAccessibilityService.instance == null) {
                tvRegionInfo.text = "Önce Erişilebilirlik iznini vermelisin!"
                return@setOnClickListener
            }
            if (selectedRegion == null) {
                tvRegionInfo.text = "Önce bir bölge seç!"
                return@setOnClickListener
            }
            val enteredInterval = etInterval.text.toString().toLongOrNull()
            intervalMs = enteredInterval?.coerceAtLeast(50L) ?: 500L

            keywordList = etKeywords.text.toString()
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            if (keywordList.isEmpty()) {
                tvRegionInfo.text = "En az bir anahtar kelime gir!"
                return@setOnClickListener
            }

            val enteredMinMatches = etMinMatches.text.toString().toIntOrNull() ?: 1
            minMatches = enteredMinMatches.coerceIn(1, keywordList.size)

            isClicking = !isClicking
            if (isClicking) {
                btnToggleStart.text = "Durdur"
                clickHandler.post(clickRunnable)
            } else {
                btnToggleStart.text = "Başlat"
                clickHandler.removeCallbacks(clickRunnable)
            }
        }

        btnClosePanel.setOnClickListener {
            stopSelf()
        }

        windowManager.addView(panel, params)
    }

    private fun startRegionSelection(tvRegionInfo: TextView) {
        if (regionSelectView != null) return

        val selectView = RegionSelectView(this) { rect ->
            selectedRegion = rect
            tvRegionInfo.text = "Bölge: (${rect.left},${rect.top}) - (${rect.right},${rect.bottom})"
            windowManager.removeView(regionSelectView)
            regionSelectView = null
        }
        regionSelectView = selectView

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            windowType(),
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START

        windowManager.addView(selectView, params)
    }

    override fun onDestroy() {
        isClicking = false
        clickHandler.removeCallbacks(clickRunnable)
        controlPanelView?.let { runCatching { windowManager.removeView(it) } }
        regionSelectView?.let { runCatching { windowManager.removeView(it) } }
        super.onDestroy()
    }
}
