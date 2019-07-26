package ru.test.library

import android.graphics.RectF
import android.widget.OverScroller
import ru.test.library.Constants.SCROLL_TO_DEFAULT_DURATION

internal class ScrollToDefaultAnimationRunnable(private val imageView: ScaleImageView,
                                                private val drawableRect:RectF,
                                                private val dragByY:(dy:Float)->Unit ):Runnable{

    private var mScroller: OverScroller = OverScroller(imageView.context)
    private var mCurrentY: Int = 0


    fun startSroll(needY:Float){
        val dy =  needY - drawableRect.centerY()
        mCurrentY = drawableRect.centerY().toInt()
        mScroller.startScroll(0,drawableRect.centerY().toInt(),0,-dy.toInt(),SCROLL_TO_DEFAULT_DURATION)
    }


    override fun run() {
        if (mScroller.isFinished) {
            return // remaining post that should not be handled
        }
        if (mScroller.computeScrollOffset()) {
            val newY = mScroller.currY
            dragByY((mCurrentY-newY).toFloat())
            mCurrentY = newY
            // Post On animation
            imageView.postOnAnimation( this)
        }
    }

}