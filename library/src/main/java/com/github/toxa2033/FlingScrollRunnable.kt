package com.github.toxa2033

import android.graphics.RectF
import android.widget.OverScroller

internal class FlingScrollRunnable(private val imageView: ScaleImageView,
                                   private val imageRectF:RectF,
                                   private val isDragToDismiss: Boolean,
                                   private val checkBoundsImageAndApplyMove:(dx:Float,dy:Float,isForScale:Boolean,isForDragToDismiss:Boolean)->Unit,
                                   private val endOfAnimation:()->Unit = {}):Runnable{

    private var mScroller: OverScroller = OverScroller(imageView.context)
    private var mCurrentX: Int = 0
    private var mCurrentY: Int = 0



    fun cancelFling() {
        mScroller.forceFinished(true)
    }

    fun fling(
        viewWidth: Int, viewHeight: Int, velocityX: Int,
        velocityY: Int
    ) {
        val rect = imageRectF
        val startX = Math.round(-rect.left)
        val minX: Int
        val maxX: Int
        val minY: Int
        val maxY: Int
        if (viewWidth < rect.width()) {
            minX = 0
            maxX = Math.round(rect.width() - viewWidth)
        } else {
            maxX = startX
            minX = maxX
        }
        val startY = Math.round(-rect.top)
        if (viewHeight < rect.height()) {
            minY = 0
            maxY = Math.round(rect.height() - viewHeight)
        } else {
            maxY = startY
            minY = maxY
        }
        mCurrentX = startX
        mCurrentY = startY
        // If we actually can move, fling the scroller
        if (startX != maxX || startY != maxY*2) {
            mScroller.fling(
                startX, startY, velocityX, velocityY/2, minX,
                maxX, minY, maxY*2, 0, 0
            )
        }
    }


    override fun run() {
        if (mScroller.isFinished) {
            endOfAnimation()
            return // remaining post that should not be handled
        }
        if (mScroller.computeScrollOffset()) {
            val newX = mScroller.currX
            val newY = mScroller.currY
            checkBoundsImageAndApplyMove((mCurrentX - newX).toFloat(),(mCurrentY - newY).toFloat(),false,isDragToDismiss)
            mCurrentX = newX
            mCurrentY = newY
            // Post On animation
            imageView.postOnAnimation( this)
        }
    }

}