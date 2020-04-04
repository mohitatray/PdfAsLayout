package com.mohitatray.pdfaslayout.layouts

import android.graphics.Paint
import com.mohitatray.pdfaslayout.Printer

class Box(
    private val fillColor: Int
): Layout() {

    private var sizeAvailable: Size? = null
    private var paint: Paint? = null

    private val isInit: Boolean
    get() = sizeAvailable != null && paint != null

    override fun init(sizeAvailable: Size) {
        require(!isInit) { "Layout should not be initialized more than once" }
        this.sizeAvailable = sizeAvailable
        paint = Paint().apply { color = fillColor }
    }

    override fun measureWidth() = sizeAvailable!!.width
    override fun measureHeight() = sizeAvailable!!.height

    override fun onDraw(
        printer: Printer,
        shouldWrapWidth: Boolean,
        shouldWrapHeight: Boolean
    ): Size {
        require(isInit) { "Layout should be initialized first" }
        val width = sizeAvailable!!.width
        val height = sizeAvailable!!.height
        printer.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint!!)
        return sizeAvailable!!
    }

    override fun destroy() {
        sizeAvailable = null
        paint = null
    }
}