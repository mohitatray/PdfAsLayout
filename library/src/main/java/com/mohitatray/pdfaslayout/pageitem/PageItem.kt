package com.mohitatray.pdfaslayout.pageitem

import com.mohitatray.pdfaslayout.layouts.Layout

sealed class PageItem

class Content(
    val layout: Layout,
    val width: Int = DEFAULT_VALUE,
    val height: Int = DEFAULT_VALUE
) : PageItem() {
    companion object {
        const val DEFAULT_VALUE = -1
    }
}

class Indentation(val left: Int, val right: Int): PageItem()

class VerticalSpace(val height: Int) : PageItem()