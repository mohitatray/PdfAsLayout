package com.mohitatray.pdfaslayout.layouts

import com.mohitatray.pdfaslayout.Printer

abstract class Layout {
    class Size(val width: Int, val height: Int)

    abstract fun init(sizeAvailable: Size)
    abstract fun measureWidth(): Int
    abstract fun measureHeight(): Int
    abstract fun onDraw(printer: Printer, shouldWrapWidth: Boolean, shouldWrapHeight: Boolean): Size
    abstract fun destroy()
}