package com.github.toxa2033

import android.annotation.SuppressLint
import android.graphics.Matrix
import android.graphics.RectF
import android.view.View
import android.widget.ImageView
import com.github.toxa2033.Constants.MAX_RATE_DISMISS
import com.github.toxa2033.Constants.MIN_RATE_DISMISS
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

internal class ScaleImageHelper(private val imageView: ScaleImageView) : IScaleImage {

    var mScaleType: ImageView.ScaleType = ImageView.ScaleType.FIT_CENTER
        set(value) {
            if (field != value) {
                field = value
                makeImage()
            }
        }

    private lateinit var defaultRect: RectF

    private var dismissRate = 0f
        set(value) {
            if (field != value) {
                field = value
                onDismissRateChange(value, false)
            }
        }

    private var currentViewY = 0f
    private var yToDismiss = 0f
    private var currentFlingRunnable: FlingScrollRunnable? = null

    var onDismissRateChange: (dismissRate: Float, isCanNowDismiss: Boolean) -> Unit = { _, _ -> }
    var onZoomChange: (currentScale: Float, minScale: Float,maxScale: Float) -> Unit = { _, _, _ -> }
    var isDismissEnabled = true
    var isZoomEnabled = true

    private val mMatrixValues = FloatArray(9)
    private lateinit var imageViewRectF: RectF

    private var suppMatrix = Matrix()
    private var middlewareMatrix = Matrix()
    private var gestures = CustomGestures(imageView.context, this)

    private var maxScale = 0f
    private var minScale = 0f
    private var midScale = 0f

    private var currentZoom:Float = 0f
    set(value) {
        if(value!=field){
            field = value
            onZoomChange(currentZoom,minScale,maxScale)
        }
    }

    override fun getMaxScale() = maxScale

    override fun getMinScale() = minScale

    override fun getMidScale() = midScale

    private val onLayoutChangeListener = View.OnLayoutChangeListener { view, _, _, _, _, _, _, _, _ ->
        imageViewRectF = RectF(view.left.toFloat(), view.top.toFloat(), view.right.toFloat(), view.bottom.toFloat())
    }

    @SuppressLint("ClickableViewAccessibility")
    fun makeImage() {
        imageView.drawable?.let {
            imageView.post {
                suppMatrix = getDefaultMatrix()
                defaultRect = getMatrixBounds(suppMatrix)
                middlewareMatrix = getDefaultMatrix()
                imageView.imageMatrix = suppMatrix

                currentViewY = 0f
                yToDismiss = imageView.measuredHeight / 4f
                minScale = getScale()
                currentZoom = minScale
                midScale = minScale * 2
                maxScale = midScale * 2

            }
            imageView.setOnTouchListener(gestures)

            imageView.removeOnLayoutChangeListener(onLayoutChangeListener)
            imageView.addOnLayoutChangeListener(onLayoutChangeListener)
        }
    }

    private fun getDefaultMatrix(): Matrix {
        val dWidth = imageView.drawable.intrinsicWidth
        val dHeight = imageView.drawable.intrinsicHeight
        val vWidth = imageView.measuredWidth
        val vHeight = imageView.measuredHeight
        val widthScale = vWidth.toFloat() / dWidth.toFloat()
        val heightScale = vHeight.toFloat() / dHeight.toFloat()

        val matrix = Matrix()

        when (mScaleType) {
            ImageView.ScaleType.CENTER -> {
                matrix.postTranslate(
                    (vWidth - dWidth) / 2f,
                    (vHeight - dHeight) / 2f
                )
            }
            ImageView.ScaleType.CENTER_CROP -> {
                val scale = max(widthScale, heightScale)
                matrix.postScale(scale, scale)
                matrix.postTranslate(
                    (vWidth - dWidth * scale) / 2f,
                    (vHeight - dHeight * scale) / 2f
                )
            }
            ImageView.ScaleType.CENTER_INSIDE -> {
                val scale = Math.min(1f, Math.min(widthScale, heightScale))
                matrix.postScale(scale, scale)
                matrix.postTranslate(
                    (vWidth - dWidth * scale) / 2f,
                    (vHeight - dHeight * scale) / 2f
                )
            }
            else -> {
                val mTempSrc = RectF(0f, 0f, dWidth.toFloat(), dHeight.toFloat())
                val mTempDst = RectF(0f, 0f, vWidth.toFloat(), vHeight.toFloat())
                when (mScaleType) {
                    ImageView.ScaleType.FIT_CENTER -> {
                        matrix.setRectToRect(mTempSrc, mTempDst, Matrix.ScaleToFit.CENTER)
                    }
                    ImageView.ScaleType.FIT_START -> {
                        matrix.setRectToRect(mTempSrc, mTempDst, Matrix.ScaleToFit.START)
                    }
                    ImageView.ScaleType.FIT_END -> {
                        matrix.setRectToRect(mTempSrc, mTempDst, Matrix.ScaleToFit.END)
                    }
                    ImageView.ScaleType.FIT_XY -> {
                        matrix.setRectToRect(mTempSrc, mTempDst, Matrix.ScaleToFit.FILL)
                    }
                    else -> {}
                }
            }
        }
        return matrix
    }

    override fun dragToDismiss(dy: Float) {
        currentViewY += dy
        dismissRate = abs(currentViewY) / yToDismiss

        if (dismissRate > MAX_RATE_DISMISS) dismissRate = MAX_RATE_DISMISS
        if (dismissRate < MIN_RATE_DISMISS) dismissRate = MIN_RATE_DISMISS

        suppMatrix.postTranslate(0f, dy)
        imageView.imageMatrix = suppMatrix

        middlewareMatrix.reset()
        middlewareMatrix.postConcat(suppMatrix)
    }

    override fun fling(lastX: Int, lastY: Int, vX: Int, vY: Int, isDragToDismiss: Boolean) {
        if (isDragToDismiss && !isDismissEnabled) return

        val displayRectF = RectF(
            -imageViewRectF.right,
            -imageViewRectF.bottom,
            imageViewRectF.right,
            imageViewRectF.bottom
        )
        currentFlingRunnable = FlingScrollRunnable(
            imageView,
            if (!isDragToDismiss) getMatrixBounds(suppMatrix) else displayRectF, isDragToDismiss,
            if (!isDragToDismiss) ::checkBoundsImageAndApplyMove else ::checkBoundsImageAndApplyMoveForSwipeToDismiss
        ) {
            if (isDragToDismiss) toDefaultPosition()
        }
        currentFlingRunnable?.fling(lastX, lastY, vX, vY)
        imageView.post(currentFlingRunnable)
    }

    override fun endOfGestureZoom() {
        if (!isZoomEnabled) return
        val currScale = getScale()
        val scale = if (currScale < minScale) minScale else return
        val zoomAnimationRunnable = ZoomAnimationRunnable(
            currScale, scale,
            defaultRect.centerX(), defaultRect.centerY(), this, imageView
        )
        imageView.post(zoomAnimationRunnable)
    }

    override fun toDefaultPosition() {
        onDismissRateChange(dismissRate, dismissRate == MAX_RATE_DISMISS)

        if(dismissRate != MAX_RATE_DISMISS) {
            val scrollToDefault = ScrollToDefaultAnimationRunnable(
                imageView,
                getMatrixBounds(suppMatrix),
                ::dragToDismiss
            )
            scrollToDefault.startSroll(defaultRect.centerY())
            imageView.post(scrollToDefault)
        }
    }

    override fun cancelFling() {
        currentFlingRunnable?.cancelFling()
    }

    override fun scaleImage(scaleFactor: Float, x: Float, y: Float): Boolean {
        if (!isZoomEnabled) return false
        suppMatrix.postScale(scaleFactor, scaleFactor, x, y)
        middlewareMatrix.reset()
        middlewareMatrix.postConcat(suppMatrix)
        checkBoundsImageAndApplyMove(0f, 0f, true)
        imageView.imageMatrix = suppMatrix
        currentZoom = getScale()
        return true
    }

    override fun scaleByDoubleTap(scale: Float, x: Float, y: Float): Boolean {
        if (!isZoomEnabled) return false
        val zoomAnimationRunnable = ZoomAnimationRunnable(getScale(), scale, x, y, this, imageView)
        imageView.post(zoomAnimationRunnable)
        return true
    }


    private fun getScaleFromMatrix(matrix: Matrix): Float {
        return sqrt(
            (Math.pow(getValue(suppMatrix, Matrix.MSCALE_X), 2.0).toFloat() + Math.pow(
                getValue(
                    matrix,
                    Matrix.MSKEW_Y
                ), 2.0
            ).toFloat()).toDouble()
        ).toFloat()
    }

    override fun getScale(): Float {
        return getScaleFromMatrix(suppMatrix).roundToDigits(5)
    }

    override fun isCanScroll(): Boolean {
        val rect = getMatrixBounds(suppMatrix)
        return imageView.measuredWidth < rect.width() || imageView.measuredHeight < rect.height()
    }

    override fun isCanDragToDismiss(): Boolean {
        val rect = getMatrixBounds(suppMatrix)
        val scale = getScale()
        return scale <= minScale && (rect.bottom <= imageViewRectF.bottom || rect.top >= imageViewRectF.top) && isDismissEnabled
    }

    private fun getValue(matrix: Matrix, whichValue: Int): Double {
        matrix.getValues(mMatrixValues)
        return mMatrixValues[whichValue].toDouble()
    }

    override fun scrollImage(dx: Float, dy: Float) {
        checkBoundsImageAndApplyMove(dx, dy)
    }

    private fun checkBoundsImageAndApplyMoveForSwipeToDismiss(
        dx: Float = 0f,
        dy: Float = 0f,
        isForScale: Boolean = false,
        isForDragToDismiss: Boolean = false
    ) {
        dragToDismiss(dy)
    }

    private fun checkBoundsImageAndApplyMove(
        dx: Float = 0f,
        dy: Float = 0f,
        isForScale: Boolean = false,
        isForDragToDismiss: Boolean = false
    ) {

        middlewareMatrix.postTranslate(dx, dy)

        val rect = getMatrixBounds(middlewareMatrix)

        var tempDx = dx
        var tempDy = dy

        val heightDrawable = rect.bottom - rect.top
        val widthDrawable = rect.right - rect.left

        val centerYDefault = defaultRect.centerY()
        val centerYCurrent = rect.centerY()

        val centerXDefault = defaultRect.centerX()
        val centerXCurrent = rect.centerX()

        if (heightDrawable < imageView.measuredHeight) {
            if (isForScale) {
                tempDy = centerYDefault - centerYCurrent
            } else {
                tempDy = 0f
            }
        } else {
            //check top and bottom borders for out of bounds in dy scroll
            if (!isCanDragToDismiss() || !isForDragToDismiss) {
                if (rect.top > imageViewRectF.top) {
                    tempDy -= imageViewRectF.top + rect.top
                } else if (rect.bottom < imageViewRectF.bottom) {
                    tempDy += imageViewRectF.bottom - rect.bottom
                }
            }
        }

        if (widthDrawable < imageView.measuredWidth) {
            if (isForScale) {
                tempDx = centerXDefault - centerXCurrent
            } else {
                tempDx = 0f
            }
        } else {
            //check left and right borders for out of bounds in dx scroll
            if (rect.left > imageViewRectF.left) {
                tempDx -= imageViewRectF.left + rect.left
            } else if (rect.right < imageViewRectF.right) {
                tempDx += imageViewRectF.right - rect.right
            }
        }

        if (tempDx != 0f || tempDy != 0f) {
            suppMatrix.postTranslate(tempDx, tempDy)
            imageView.imageMatrix = suppMatrix
        }

        middlewareMatrix.reset()
        middlewareMatrix.postConcat(suppMatrix)
    }

    private fun getMatrixBounds(matrix: Matrix): RectF {
        val bounds = RectF()
        val drawable = imageView.drawable
        if (drawable != null) {
            matrix.mapRect(bounds, RectF(drawable.bounds))
        }
        return bounds
    }

}