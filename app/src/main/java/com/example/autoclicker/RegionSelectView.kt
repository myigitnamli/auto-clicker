package com.example.autoclicker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View

/**
 * Kullanıcının parmağını ekranda sürükleyerek bir dikdörtgen çizmesini sağlayan
 * tam ekran şeffaf katman. Sürükleme bittiğinde seçilen bölge onCompleted ile
 * dışarı bildirilir.
 */
class RegionSelectView(
    context: Context,
    private val onCompleted: (Rect) -> Unit
) : View(context) {

    private var startX = 0f
    private var startY = 0f
    private var currentX = 0f
    private var currentY = 0f
    private var dragging = false

    private val fillPaint = Paint().apply {
        color = Color.parseColor("#5500E5FF")
        style = Paint.Style.FILL
    }

    private val borderPaint = Paint().apply {
        color = Color.parseColor("#0091EA")
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    private val dimPaint = Paint().apply {
        color = Color.parseColor("#66000000")
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Tüm ekranı hafif karart, seçim yapılmasını görsel olarak belirginleştir
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), dimPaint)

        if (dragging) {
            val left = minOf(startX, currentX)
            val top = minOf(startY, currentY)
            val right = maxOf(startX, currentX)
            val bottom = maxOf(startY, currentY)
            canvas.drawRect(left, top, right, bottom, fillPaint)
            canvas.drawRect(left, top, right, bottom, borderPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.rawX
                startY = event.rawY
                currentX = startX
                currentY = startY
                dragging = true
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                currentX = event.rawX
                currentY = event.rawY
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                dragging = false
                val left = minOf(startX, currentX).toInt()
                val top = minOf(startY, currentY).toInt()
                val right = maxOf(startX, currentX).toInt()
                val bottom = maxOf(startY, currentY).toInt()
                invalidate()
                onCompleted(Rect(left, top, right, bottom))
            }
        }
        return true
    }
}
