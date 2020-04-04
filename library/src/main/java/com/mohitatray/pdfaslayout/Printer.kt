package com.mohitatray.pdfaslayout

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.StaticLayout

class Printer(private val canvas: Canvas) {

    var translateX = 0f
        private set
    var translateY = 0f
        private set

    fun translate(x: Float, y: Float) {
        canvas.translate(x, y)
        translateX += x
        translateY += y
    }

    fun setPosition(x: Float, y: Float) {
        translate(x-translateX, y-translateY)
    }

    fun drawRect(left: Float, top: Float, right: Float, bottom: Float, paint: Paint) {
        canvas.drawRect(left, top, right, bottom, paint)
    }

    fun drawStaticLayout(staticLayout: StaticLayout) {
        staticLayout.draw(canvas)
    }

    fun drawDrawable(drawable: Drawable) {
        drawable.draw(canvas)
    }

}