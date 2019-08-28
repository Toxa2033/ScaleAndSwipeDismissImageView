package com.github.toxa2033

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.widget.ImageView
import android.graphics.drawable.Drawable
import android.net.Uri


class ScaleImageView : ImageView {

    private var waitingScaleType: ScaleType? = null
    private lateinit var scaleImageHelper: ScaleImageHelper

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }


    private fun init(attrs: AttributeSet?=null) {
        super.setScaleType(ScaleType.MATRIX)
        scaleImageHelper = ScaleImageHelper(this)
        if (waitingScaleType != null) {
            scaleImageHelper.mScaleType = waitingScaleType!!
            waitingScaleType = null
        }
        scaleImageHelper.makeImage()

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.ScaleImageView)
            val zoomEnabled = typedArray.getBoolean(R.styleable.ScaleImageView_zoom_enabled, true)
            val dismissEnabled = typedArray.getBoolean(R.styleable.ScaleImageView_dismiss_enabled, true)
            setZoomEnabled(zoomEnabled)
            setDismissEnabled(dismissEnabled)
            typedArray.recycle()
        }

    }

    override fun setImageBitmap(bm: Bitmap?) {
        super.setImageBitmap(bm)
        scaleImageHelper.makeImage()
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        scaleImageHelper.makeImage()
    }

    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
        scaleImageHelper.makeImage()
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        scaleImageHelper.makeImage()
    }

    override fun setFrame(l: Int, t: Int, r: Int, b: Int): Boolean {
        val changed = super.setFrame(l, t, r, b)
        if (changed) {
            scaleImageHelper.makeImage()
        }
        return changed
    }


    override fun setScaleType(scaleType: ScaleType) {
        if (!::scaleImageHelper.isInitialized) {
            waitingScaleType = scaleType
        } else {
            scaleImageHelper.mScaleType = scaleType
        }
    }

    override fun getScaleType() = scaleImageHelper.mScaleType

    fun setZoomEnabled(enable: Boolean) {
        scaleImageHelper.isZoomEnabled = enable
    }

    fun setDismissEnabled(enable: Boolean) {
        scaleImageHelper.isDismissEnabled = enable
    }

    fun setOnDismissRateChange(onDismissRateChange: (rate: Float, isCanNowDismiss: Boolean) -> Unit) {
        scaleImageHelper.onDismissRateChange = onDismissRateChange
    }

    fun setOnZoomChange(onZoomChange: (currentScale: Float, minScale: Float, maxScale: Float) -> Unit) {
        scaleImageHelper.onZoomChange = onZoomChange
    }

    fun stopFlingAnimation(){
        scaleImageHelper.cancelFling()
    }
}