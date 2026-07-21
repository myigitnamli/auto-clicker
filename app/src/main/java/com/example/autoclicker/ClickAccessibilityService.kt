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
     * Seçilen bölge içindeki tıklanabilir öğeleri tarar; metni (veya içerik
     * açıklamasını) verilen anahtar kelimelerden birini içeren İLK öğeyi
     * bulunca gerçek bir "tıkla" eylemi (ACTION_CLICK) gönderir ve true
     * döner. Eşleşen öğe yoksa false.
     *
     * ACTION_CLICK, koordinata dokunmak yerine butonun kendisini tetiklediği
     * için WebView olmayan normal Android arayüzlerinde koordinat tabanlı
     * tıklamadan çok daha güvenilirdir.
     */
    fun performKeywordClick(region: Rect, keywords: List<String>): Boolean {
        if (keywords.isEmpty()) return false
        val root = rootInActiveWindow ?: return false
        val target = findMatchingNode(root, region, keywords)
        return target?.performAction(AccessibilityNodeInfo.ACTION_CLICK) ?: false
    }

    private fun findMatchingNode(
        root: AccessibilityNodeInfo,
        region: Rect,
        keywords: List<String>
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

                val matches = keywords.any { it.isNotBlank() && label.contains(it.lowercase()) }
                if (matches) {
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
