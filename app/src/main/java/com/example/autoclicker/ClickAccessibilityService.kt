package com.example.autoclicker

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

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
     * (Metin/filtre bilgisi olmayan, sadece koordinata dayalı basit tıklama.)
     */
    fun performTap(x: Float, y: Float) {
        val path = Path().apply { moveTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(path, 0L, 40L)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        dispatchGesture(gesture, null, null)
    }

    /**
     * Seçilen bölge içindeki TÜM öğelerin metnini (sadece tıklanabilir olanları
     * değil) tarar ve verilen anahtar kelimelerden kaç farklısının ekranda
     * geçtiğini sayar. Eğer bu sayı `minMatches` değerine ulaşırsa VE bölgede
     * bu kelimelerden birini içeren tıklanabilir bir öğe bulunduysa, o öğeye
     * gerçek bir "tıkla" eylemi (ACTION_CLICK) gönderir.
     *
     * Örnek: keywords = ["Araba", "Onay"], minMatches = 2 ise; ekranda sadece
     * "Onay" yazan bir buton olması yetmez, aynı anda "Araba" kelimesi de
     * (örn. dialog metninde) geçmelidir. İkisi de geçtiğinde "Onay" butonuna
     * tıklanır.
     */
    fun performKeywordClick(region: Rect, keywords: List<String>, minMatches: Int): Boolean {
        if (keywords.isEmpty()) return false
        val root = rootInActiveWindow ?: return false

        val matchedKeywords = mutableSetOf<String>()
        var clickTarget: AccessibilityNodeInfo? = null

        val stack = ArrayDeque<AccessibilityNodeInfo>()
        stack.addLast(root)
        val bounds = Rect()

        while (stack.isNotEmpty()) {
            val node = stack.removeLast()
            node.getBoundsInScreen(bounds)

            if (Rect.intersects(bounds, region)) {
                val label = (node.text?.toString() ?: node.contentDescription?.toString() ?: "")
                    .lowercase()

                for (keyword in keywords) {
                    if (keyword.isNotBlank() && label.contains(keyword.lowercase())) {
                        matchedKeywords.add(keyword.lowercase())
                        if (node.isClickable && clickTarget == null) {
                            clickTarget = node
                        }
                    }
                }
            }

            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { stack.addLast(it) }
            }
        }

        val target = clickTarget
        return if (matchedKeywords.size >= minMatches && target != null) {
            target.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        } else {
            false
        }
    }
}
