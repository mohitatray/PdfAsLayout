package com.mohitatray.pdfaslayout.layouts

import android.os.Build
import android.text.StaticLayout
import android.text.TextPaint
import com.mohitatray.pdfaslayout.Printer
import kotlin.math.max
import kotlin.math.min

class Text(
    private val text: String,
    private val textPaint: TextPaint,
    private val alignment: Alignment = Alignment.LEFT,
    private val verticalAlignment: VerticalAlignment = VerticalAlignment.TOP,
    private val maxWidth: Int = -1
) : Layout() {

    enum class Alignment {
        LEFT, RIGHT, CENTER
    }

    enum class VerticalAlignment {
        TOP, CENTER, BOTTOM
    }

    private var staticLayout: StaticLayout? = null
    private var sizeAvailable: Size? = null
    private var measuredWidth: Int? = null

    private val isInit
    get() = staticLayout != null && sizeAvailable != null

    override fun init(sizeAvailable: Size) {
        require(!isInit) { "init cannot be called more than once" }
        val width = if (maxWidth < 0) sizeAvailable.width else min(sizeAvailable.width, maxWidth)
        this.sizeAvailable = sizeAvailable
        staticLayout = obtainStaticLayout(
            text,
            width,
            textPaint,
            when (alignment) {
                Alignment.LEFT -> android.text.Layout.Alignment.ALIGN_NORMAL
                Alignment.CENTER -> android.text.Layout.Alignment.ALIGN_CENTER
                Alignment.RIGHT -> android.text.Layout.Alignment.ALIGN_OPPOSITE
            }
        )
    }

    override fun measureWidth(): Int {
        require(isInit) { "Layout must be initialized first" }
        return measuredWidth.let {
            if (it != null) it
            else {
                val width = getCalculatedWidth(staticLayout!!)
                measuredWidth = width
                width
            }
        }
    }

    override fun measureHeight(): Int {
        require(isInit) { "Layout must be initialized first" }
        return staticLayout!!.height
    }

    override fun onDraw(
        printer: Printer,
        shouldWrapWidth: Boolean,
        shouldWrapHeight: Boolean
    ): Size {
        require(isInit) { "Layout must be initialized first" }
        val measuredWidth = measureWidth()
        val measuredHeight = measureHeight()
        val availableWidth = sizeAvailable!!.width
        val availableHeight = sizeAvailable!!.height
        val consumedWidth = if (shouldWrapWidth) measuredWidth else availableWidth
        val consumedHeight = if (shouldWrapHeight) measuredHeight else availableHeight

        val staticLayoutWidth = if (maxWidth < 0) availableWidth else min(availableWidth, maxWidth)

        // Translate printer to top-left
        val xTranslate = if (shouldWrapWidth) {
            when (alignment) {
                Alignment.LEFT -> 0f
                Alignment.CENTER -> (measuredWidth - staticLayoutWidth) / 2f
                Alignment.RIGHT -> (measuredWidth - staticLayoutWidth).toFloat()
            }
        }
        else {
            when (alignment) {
                Alignment.LEFT -> 0f
                Alignment.CENTER -> (availableWidth - staticLayoutWidth) / 2f
                Alignment.RIGHT -> (availableWidth - staticLayoutWidth).toFloat()
            }
        }

        val yTranslate = when (verticalAlignment) {
            VerticalAlignment.TOP -> 0f
            VerticalAlignment.CENTER -> (consumedHeight - measuredHeight) / 2f
            VerticalAlignment.BOTTOM -> (consumedHeight - measuredHeight).toFloat()
        }
        printer.translate(xTranslate, yTranslate)

        // Draw the content
        printer.drawStaticLayout(staticLayout!!)

        return Size(consumedWidth, consumedHeight)
    }

    override fun destroy() {
        staticLayout = null
        sizeAvailable = null
        measuredWidth = null
    }

    private fun obtainStaticLayout(
        text: String,
        width: Int,
        textPaint: TextPaint,
        alignment: android.text.Layout.Alignment
    ): StaticLayout {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val staticLayoutBuilder = StaticLayout.Builder.obtain(text, 0, text.length, textPaint, width)
                .setAlignment(alignment)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                staticLayoutBuilder.setUseLineSpacingFromFallbacks(true)
            staticLayoutBuilder.build()
        } else {
            @Suppress("DEPRECATION")
            (StaticLayout(text, textPaint, width, alignment, 1f, 0f, true))
        }
    }

    private fun getCalculatedWidth(staticLayout: StaticLayout): Int {
        val lineCount = staticLayout.lineCount
        var maxWidth = 0
        for (i in 0 until lineCount) maxWidth = max(maxWidth, staticLayout.getLineWidth(i).toInt())
        return maxWidth
    }

}