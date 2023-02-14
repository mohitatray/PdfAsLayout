package com.mohitatray.pdfaslayout.layouts

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.DrawableRes
import com.mohitatray.pdfaslayout.Printer

class Image(private val drawable: Drawable) : Layout() {

    @Suppress("DEPRECATION")
    constructor(context: Context, @DrawableRes drawableRes: Int) : this(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            context.getDrawable(drawableRes)!!
        else context.resources.getDrawable(drawableRes)
    )

    private var sizeAvailable: Size? = null
    private val isInit
    get() = sizeAvailable != null

    override fun init(sizeAvailable: Size) {
        require(!isInit) { "init can only be called once" }
        this.sizeAvailable = sizeAvailable
        drawable.setBounds(0, 0, sizeAvailable.width, sizeAvailable.height)
    }

    override fun measureWidth(): Int {
        require(isInit) { "Layout must be initialized first" }
        return sizeAvailable!!.width
    }

    override fun measureHeight(): Int {
        require(isInit) { "Layout must be initialized first" }
        return sizeAvailable!!.height
    }

    override fun onDraw(
        printer: Printer,
        shouldWrapWidth: Boolean,
        shouldWrapHeight: Boolean
    ): Size {
        require(isInit) { "Layout must be initialized first" }
        printer.drawDrawable(drawable)
        return sizeAvailable!!
    }

    override fun destroy() {
        sizeAvailable = null
    }

}