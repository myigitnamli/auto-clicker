package com.example.autoclicker

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent

/**
 * Erişilebilirlik servisi: sistem seviyesinde gerçek bir dokunma (tap) gesture'ı
 * göndermek için gereklidir. Android, uygulamaların başka uygulamalara programatik
 * olarak dokunmasına normalde izin vermez; bu iznin kullanıcı tarafından
 * Ayarlar > Erişilebilirlik menüsünden açıkça verilmesi gerekir.
 */
class ClickAccessibilityService : AccessibilityService() {

    companion object {
        // OverlayService'in bu servise erişip tıklama komutu gönderebilmesi için
        var instance: ClickAccessibilityService? = null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Ekran içeriğini okumuyoruz, sadece tıklama gönderiyoruz.
    }

    override fun onInterrupt() {}

    /**
     * Verilen ekran koordinatında kısa bir "tap" gesture'ı gönderir.
     */
    fun performTap(x: Float, y: Float) {
        val path = Path().apply { moveTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(path, 0L, 40L)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        dispatchGesture(gesture, null, null)
    }
}
