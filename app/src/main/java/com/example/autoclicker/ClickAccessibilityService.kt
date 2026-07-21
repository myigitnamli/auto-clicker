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
     * Seçilen bölge içindeki tıklanabilir öğeleri tarar; metni (veya
     * içerik açıklamasını) blacklist'te geçen bir kelime içeriyorsa o öğeyi
     * ATLAR. whitelist doluysa, sadece whitelist'teki kelimelerden birini
     * içeren öğelere tıklar. Uygun ilk öğeyi bulunca gerçek bir "tıkla"
     * eylemi (ACTION_CLICK) gönderir ve true döner. Uygun öğe yoksa false.
     *
     * ACTION_CLICK, koordinata dokunmak yerine butonun kendisini tetiklediği
     * için WebView olmayan normal Android arayüzlerinde koordinat tabanlı
     * tıklamadan çok daha güvenilirdir.
     */
    fun performFilteredClick(region: Rect, blacklist: List<String>, whitelist: List<String>): Boolean {
        val root = rootInActiveWindow ?: return false
        val target = findMatchingNode(root, region, blacklist, whitelist)
        return if (target != null) {
            val result = target.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            result
        } else {
            false
        }
    }

    private fun findMatchingNode(
        root: AccessibilityNodeInfo,
        region: Rect,
        blacklist: List<String>,
        whitelist: List<String>
    ): AccessibilityNodeInfo? {
        val stack = ArrayDeque<AccessibilityNodeInfo>()
        stack.addLast(root)
        val bounds = Rect()

        while (stack.isNotEmpty()) {
            val node = stack.removeLast()

            node.getBoundsInScreen(bounds)
            if (node.isClickable && Rect.intersects(bounds, region)) {
                val label = (node.text?.toString() ?: node.contentDescription?.toString() ?: "")
                    .lowercase()

                val isBlacklisted = blacklist.any { it.isNotBlank() && label.contains(it.lowercase()) }
                val passesWhitelist = whitelist.isEmpty() ||
                    whitelist.any { it.isNotBlank() && label.contains(it.lowercase()) }

                if (!isBlacklisted && passesWhitelist) {
                    return node
                }
            }

            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { stack.addLast(it) }
            }
        }
        return null
    }
}
