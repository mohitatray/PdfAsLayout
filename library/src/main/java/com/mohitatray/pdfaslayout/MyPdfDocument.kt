package com.mohitatray.pdfaslayout

import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import com.mohitatray.pdfaslayout.layouts.Layout
import com.mohitatray.pdfaslayout.pageitem.Content
import com.mohitatray.pdfaslayout.pageitem.Indentation
import com.mohitatray.pdfaslayout.pageitem.PageItem
import com.mohitatray.pdfaslayout.pageitem.VerticalSpace
import java.io.OutputStream
import kotlin.math.min

class MyPdfDocument(
    private val pageWidth: Int,
    private val pageHeight: Int,
    private val headerContent: Layout? = null,
    private val footerContent: Layout? = null,
    private val pageItems: List<PageItem>,
    private val margins: Rect = Rect()
) {

    private val document = PdfDocument()
    private lateinit var page: PdfDocument.Page
    private var pageNo = 0
    private lateinit var printer: Printer
    private var remainingWidth = pageWidth - margins.left - margins.right
    private var remainingHeight = pageHeight - margins.top - margins.bottom
    private var availableMainContentHeight = 0

    val totalPages get() = pageNo

    init {
        headerContent?.init(Layout.Size(remainingWidth, remainingHeight))
        footerContent?.init(Layout.Size(remainingWidth, remainingHeight))
        startNewPage()
        drawMainContent()
        finishDocument()
    }

    private fun createNewPage() {
        if (::printer.isInitialized) {
            printer.setPosition(0f, 0f)
            document.finishPage(page)
        }
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, ++pageNo).create()
        page = document.startPage(pageInfo)
        printer = Printer(page.canvas)
        printer.setPosition(margins.left.toFloat(), margins.top.toFloat())
        remainingHeight = pageHeight - margins.top - margins.bottom
    }

    private fun startNewPage() {
        val initialXPosition = if (::printer.isInitialized) printer.translateX
        else margins.left.toFloat()
        createNewPage()
        drawHeader()
        if (footerContent != null) {
            printer.setPosition(
                margins.left.toFloat(),
                margins.top.toFloat() + remainingHeight - footerContent.measureHeight()
            )
            drawFooter()
        }
        val headerHeight = (headerContent?.measureHeight() ?: 0)
        remainingHeight -= (headerHeight + (footerContent?.measureHeight() ?: 0))
        printer.setPosition(initialXPosition, (margins.top + headerHeight).toFloat())
        availableMainContentHeight = remainingHeight
    }

    private fun drawHeader() {
        headerContent?.onDraw(printer, false, shouldWrapHeight = true)
    }

    private fun drawFooter() {
        footerContent?.onDraw(printer, false, shouldWrapHeight = true)
    }

    private fun drawMainContent() {
        pageItems.forEach { pageItem ->
            when (pageItem) {
                is Content -> {
                    val layout = pageItem.layout

                    // Calculate available width and height of content
                    val availableWidth = if (pageItem.width < 0) remainingWidth
                    else min(pageItem.width, remainingWidth)

                    val availableHeight = if (pageItem.height < 0) availableMainContentHeight
                    else min(pageItem.height, availableMainContentHeight)

                    // Init layout with available size
                    layout.init(Layout.Size(availableWidth, availableHeight))

                    // Start new page if content height exceeds remaining height
                    val contentHeight = layout.measureHeight()
                    if (remainingHeight < contentHeight) startNewPage()

                    // Store translate
                    val translateX = printer.translateX
                    val translateY = printer.translateY

                    // Draw the content
                    layout.onDraw(printer, false, shouldWrapHeight = true)
                    layout.destroy()

                    // Set Y position below this content and restore X position
                    printer.setPosition(translateX, translateY + contentHeight)

                    // Subtract content height from remaining height
                    remainingHeight -= contentHeight
                }

                is Indentation -> {
                    indentLeft(pageItem.left)
                    indentRight(pageItem.right)
                }

                is VerticalSpace -> {
                    val height = pageItem.height
                    if (remainingHeight < height) startNewPage()
                    else {
                        printer.translate(0f, height.toFloat())
                        remainingHeight -= height
                    }
                }
            }
        }
    }

    private fun indentLeft(value: Int) {
        printer.translate(value.toFloat(), 0f)
        remainingWidth -= value
    }

    private fun indentRight(value: Int) {
        remainingWidth -= value
    }

    private fun finishDocument() {
        document.finishPage(page)
        headerContent?.destroy()
        footerContent?.destroy()
    }

    fun writeToStream(outputStream: OutputStream) {
        document.writeTo(outputStream)
    }

    fun close() {
        document.close()
    }

}