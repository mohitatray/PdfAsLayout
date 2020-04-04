package com.mohitatray.pdfaslayout.layouts

import com.mohitatray.pdfaslayout.Printer
import kotlin.math.max
import kotlin.math.min

class LinearContainer(
    private val childLayouts: List<Pair<Layout, SizeSpecDimensions>>,
    private val orientation: Orientation = Orientation.VERTICAL,
    private val horizontalGravity: HorizontalGravity = HorizontalGravity.LEFT,
    private val verticalGravity: VerticalGravity = VerticalGravity.TOP
): Layout() {

    enum class Orientation {
        HORIZONTAL, VERTICAL, OVER
    }

    enum class HorizontalGravity {
        LEFT, CENTER, RIGHT
    }

    enum class VerticalGravity {
        TOP, CENTER, BOTTOM
    }

    data class SizeSpecDimensions(val widthSpec: SizeSpec, val heightSpec: SizeSpec)

    abstract class SizeSpec
    class AbsoluteSizeSpec(val value: Int): SizeSpec()
    class RelativeSizeSpec(val spec: Spec): SizeSpec() {
        enum class Spec {
            WRAP_CONTENT, MATCH_PARENT, FILL_REMAINING
        }
    }

    private val consumedChildWidths = IntArray(childLayouts.size) {-1}
    private val consumedChildHeights = IntArray(childLayouts.size) {-1}
    private var sizeAvailable: Size? = null
    private val isInit
    get() = sizeAvailable != null
    private var measuredSize: Size? = null

    override fun init(sizeAvailable: Size) {
        require(!isInit) { "Layout must be initialized only once" }
        this.sizeAvailable = sizeAvailable
        measureAndInitChildLayouts()
    }

    private fun measureAndInitChildLayouts() {
        require(isInit) { "Layout must be initialized first" }
        var totalConsumedWidth = 0
        val totalMaxWidth = sizeAvailable!!.width
        var totalConsumedHeight = 0
        val totalMaxHeight = sizeAvailable!!.height
        var fillLayoutPosition = -1

        fun processChild(index: Int, layout: Layout, specDimens: SizeSpecDimensions, isFillRemainingMode: Boolean) {
            val (widthSpec, heightSpec) = specDimens
            val remainingWidth = if (orientation == Orientation.HORIZONTAL) totalMaxWidth - totalConsumedWidth
            else totalMaxWidth
            val remainingHeight = if (orientation == Orientation.VERTICAL) totalMaxHeight - totalConsumedHeight
            else totalMaxHeight

            val availableWidth =
                when (widthSpec) {
                    is AbsoluteSizeSpec -> min(remainingWidth, widthSpec.value)
                    is RelativeSizeSpec -> when(widthSpec.spec) {
                        RelativeSizeSpec.Spec.WRAP_CONTENT -> remainingWidth
                        RelativeSizeSpec.Spec.MATCH_PARENT -> remainingWidth
                        RelativeSizeSpec.Spec.FILL_REMAINING -> {
                            if (isFillRemainingMode) remainingWidth
                            else {
                                require(fillLayoutPosition < 0) { "More than 1 children having FILL_REMAINING is not allowed" }
                                require(orientation == Orientation.HORIZONTAL) { "Wrong orientation with FILL_REMAINING" }
                                fillLayoutPosition = index
                                -1
                            }
                        }
                    }
                    else -> throw IllegalArgumentException("Invalid size spec")
                }

            val availableHeight =
                when (heightSpec) {
                    is AbsoluteSizeSpec -> min(remainingHeight, heightSpec.value)
                    is RelativeSizeSpec -> when(heightSpec.spec) {
                        RelativeSizeSpec.Spec.WRAP_CONTENT -> remainingHeight
                        RelativeSizeSpec.Spec.MATCH_PARENT -> remainingHeight
                        RelativeSizeSpec.Spec.FILL_REMAINING -> {
                            if (isFillRemainingMode) remainingHeight
                            else {
                                require(fillLayoutPosition < 0) { "More than 1 children having FILL_REMAINING is not allowed" }
                                require(orientation == Orientation.VERTICAL) { "Wrong orientation with FILL_REMAINING" }
                                fillLayoutPosition = index
                                -1
                            }
                        }
                    }
                    else -> throw IllegalArgumentException("Invalid size spec")
                }

            if (availableWidth >= 0 && availableHeight >= 0) {
                layout.init(Size(availableWidth, availableHeight))

                val consumedWidth =
                    if (widthSpec is RelativeSizeSpec && widthSpec.spec == RelativeSizeSpec.Spec.WRAP_CONTENT)
                        min(availableWidth, layout.measureWidth())
                    else availableWidth
                totalConsumedWidth =
                    if (orientation == Orientation.HORIZONTAL) totalConsumedWidth + consumedWidth
                    else max(totalConsumedWidth, consumedWidth)

                val consumedHeight =
                    if (heightSpec is RelativeSizeSpec && heightSpec.spec == RelativeSizeSpec.Spec.WRAP_CONTENT)
                        min(availableHeight, layout.measureHeight())
                    else availableHeight
                totalConsumedHeight =
                    if (orientation == Orientation.VERTICAL) totalConsumedHeight + consumedHeight
                    else max(totalConsumedHeight, consumedHeight)

                consumedChildWidths[index] = consumedWidth
                consumedChildHeights[index] = consumedHeight
            }
        }

        childLayouts.forEachIndexed { i, (layout, specDimens) ->
            processChild(i, layout, specDimens, false)
        }

        // About FILL_REMAINING
        if (fillLayoutPosition >= 0) {
            val (layout, specDimens) = childLayouts[fillLayoutPosition]
            processChild(fillLayoutPosition, layout, specDimens, true)
        }

        measuredSize = Size(totalConsumedWidth, totalConsumedHeight)
    }

    override fun measureWidth() = measuredSize!!.width
    override fun measureHeight() = measuredSize!!.height

    override fun onDraw(printer: Printer, shouldWrapWidth: Boolean, shouldWrapHeight: Boolean): Size {
        val totalWidth = (if (shouldWrapWidth) measuredSize else sizeAvailable)!!.width
        val totalHeight = (if (shouldWrapHeight) measuredSize else sizeAvailable)!!.height
        val measuredWidth = measuredSize!!.width
        val measuredHeight = measuredSize!!.height

        // Initial translate to the top-left from where to start drawing layout of measuredSize size
        val initialTranslateX = when (horizontalGravity) {
            HorizontalGravity.LEFT -> 0f
            HorizontalGravity.CENTER -> (totalWidth - measuredWidth) / 2f
            HorizontalGravity.RIGHT -> (totalWidth - measuredWidth).toFloat()
        }

        val initialTranslateY = when (verticalGravity) {
            VerticalGravity.TOP -> 0f
            VerticalGravity.CENTER -> (totalHeight - measuredHeight) / 2f
            VerticalGravity.BOTTOM -> (totalHeight - measuredHeight).toFloat()
        }

        printer.translate(initialTranslateX, initialTranslateY)

        childLayouts.forEachIndexed { index, (layout, specDimens) ->
            val consumedWidth = consumedChildWidths[index]
            val consumedHeight = consumedChildHeights[index]
            val (widthSpec, heightSpec) = specDimens

            // Store the current translate
            val translateX = printer.translateX
            val translateY = printer.translateY

            // Translate to top-left of this child
            val childTranslateX = when (orientation) {
                Orientation.HORIZONTAL -> 0f
                else -> when (horizontalGravity) {
                    HorizontalGravity.LEFT -> 0f
                    HorizontalGravity.CENTER -> (measuredWidth - consumedWidth) / 2f
                    HorizontalGravity.RIGHT -> (measuredWidth - consumedWidth).toFloat()
                }
            }

            val childTranslateY = when (orientation) {
                Orientation.VERTICAL -> 0f
                else -> when (verticalGravity) {
                    VerticalGravity.TOP -> 0f
                    VerticalGravity.CENTER -> (measuredHeight - consumedHeight) / 2f
                    VerticalGravity.BOTTOM -> (measuredHeight - consumedHeight).toFloat()
                }
            }

            printer.translate(childTranslateX, childTranslateY)

            // Draw the child
            layout.onDraw(
                printer,
                widthSpec is RelativeSizeSpec && widthSpec.spec == RelativeSizeSpec.Spec.WRAP_CONTENT,
                heightSpec is RelativeSizeSpec && heightSpec.spec == RelativeSizeSpec.Spec.WRAP_CONTENT
            )

            // Restore to stored translate
            printer.setPosition(translateX, translateY)

            // Move translate according to orientation
            when (orientation) {
                Orientation.HORIZONTAL -> printer.translate(consumedWidth.toFloat(), 0f)
                Orientation.VERTICAL -> printer.translate(0f, consumedHeight.toFloat())
                else -> {}
            }
        }

        return Size(totalWidth, totalHeight)
    }

    override fun destroy() {
        childLayouts.forEach { (it, _) -> it.destroy() }
        sizeAvailable = null
        measuredSize = null
    }
}