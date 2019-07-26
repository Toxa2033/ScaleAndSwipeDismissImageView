package ru.test.library

import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.view.ViewCompat.postOnAnimation
import ru.test.library.Constants.ZOOM_DURATION
import kotlin.math.min

internal class ZoomAnimationRunnable(
    private val currentZoom:Float,
    private val targetZoom:Float,
    private val x:Float,
    private val y:Float,
    private val imageHelper: ScaleImageHelper,
    private val imageView: ScaleImageView,
    private val startTime:Long = System.currentTimeMillis()
):Runnable {

    private val interpolator = AccelerateDecelerateInterpolator()

    private fun interpolate(): Float {
        var t = 1f * (System.currentTimeMillis() - startTime) / ZOOM_DURATION
        t = min(1f, t)
        t = interpolator.getInterpolation(t)
        return t
    }


    override fun run() {
        val t = interpolate()
        val scale = currentZoom + t * (targetZoom - currentZoom)
        val deltaScale = scale / imageHelper.getScale()
        imageHelper.scaleImage(deltaScale,x,y)

        if (t < 1f) {
            postOnAnimation(imageView, this)
        }
    }
}