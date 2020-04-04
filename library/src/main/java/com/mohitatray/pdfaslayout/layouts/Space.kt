package com.mohitatray.pdfaslayout.layouts

import com.mohitatray.pdfaslayout.Printer

class Space: Layout() {

    override fun init(sizeAvailable: Size) {}
    override fun measureWidth() = 0
    override fun measureHeight() = 0

    override fun onDraw(
        printer: Printer,
        shouldWrapWidth: Boolean,
        shouldWrapHeight: Boolean
    ) = Size(0, 0)

    override fun destroy() {}

}