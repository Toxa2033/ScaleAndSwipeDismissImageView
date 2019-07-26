package com.github.toxa2033

import android.annotation.SuppressLint
import android.content.Context
import android.view.*
import com.github.toxa2033.Constants.INVALID_POINTER_ID
import kotlin.math.abs
import kotlin.math.sqrt


internal class CustomGestures(context: Context, private var iScaleImage: IScaleImage) : View.OnTouchListener,
    ScaleGestureDetector.OnScaleGestureListener,
    GestureDetector.OnDoubleTapListener {


    private var mActivePointerId = INVALID_POINTER_ID
    private var mActivePointerIndex = 0

    private var gestures = GestureDetector(context, object:GestureDetector.OnGestureListener{
        override fun onShowPress(p0: MotionEvent?) {}
        override fun onSingleTapUp(p0: MotionEvent?) = true
        override fun onDown(p0: MotionEvent?) = true
        override fun onFling(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float) = true
        override fun onScroll(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float) = true
        override fun onLongPress(p0: MotionEvent?) {}
    })
    override fun onDoubleTapEvent(p0: MotionEvent?) = true
    override fun onSingleTapConfirmed(p0: MotionEvent?) = true

    private val gestureScale = ScaleGestureDetector(context, this)
    private var isInScale = false

    fun isInScale() = isInScale

    private var mVelocityTracker: VelocityTracker? = null
    private var mIsDragging: Boolean = false
    private var mLastTouchX: Float = 0f
    private var mLastTouchY: Float = 0f
    private var mTouchSlop: Float = 0f
    private var mMinimumVelocity: Float = 0f
    private var isNowDragToDismiss = false

    init {
        gestures.setOnDoubleTapListener(this)
        val viewConfig = ViewConfiguration.get(context)
        mTouchSlop = viewConfig.scaledTouchSlop.toFloat()
        mMinimumVelocity = viewConfig.scaledMinimumFlingVelocity.toFloat()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(p0: View?, event: MotionEvent): Boolean {
        gestures.onTouchEvent(event)
        gestureScale.onTouchEvent(event)
        processTouchEvent(event)

        return true
    }

    override fun onScaleBegin(p0: ScaleGestureDetector?): Boolean {
        isInScale = true
        return true
    }

    override fun onScaleEnd(p0: ScaleGestureDetector?) {}

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        val scale = iScaleImage.getScale()

        if (scale < iScaleImage.getMaxScale() || detector.scaleFactor < 1f) {
            iScaleImage.scaleImage(detector.scaleFactor, detector.focusX, detector.focusY)
        }
        return true
    }


    override fun onDoubleTap(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        val zoom: Float

        val scale = iScaleImage.getScale()
        val minScale = iScaleImage.getMinScale()
        val midScale = iScaleImage.getMidScale()
        val maxScale = iScaleImage.getMaxScale()

        if (scale < midScale) {
            zoom = midScale
        } else if (scale >= midScale && scale < maxScale) {
            zoom = maxScale
        } else {
            zoom = minScale
        }
        iScaleImage.scaleByDoubleTap(zoom, x, y)

        return true
    }

    private fun getActiveX(ev: MotionEvent): Float {
        var x = ev.x
        try {
            x = ev.getX(mActivePointerIndex)
        } catch (e: Exception) {
        }

        return x
    }

    private fun getActiveY(ev: MotionEvent): Float {
        var y = ev.y
        try {
            y = ev.getY(mActivePointerIndex)
        } catch (e: Exception) {
        }

        return y
    }


    private fun processTouchEvent(ev: MotionEvent): Boolean {

        val action = ev.action

        when (action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                isNowDragToDismiss = false
                mActivePointerId = ev.getPointerId(0)

                mVelocityTracker = VelocityTracker.obtain()
                mVelocityTracker?.addMovement(ev)

                mLastTouchX = getActiveX(ev)
                mLastTouchY = getActiveY(ev)
                mIsDragging = false
                iScaleImage.cancelFling()
            }
            MotionEvent.ACTION_MOVE -> {
                val x = getActiveX(ev)
                val y = getActiveY(ev)
                val dx = x - mLastTouchX
                val dy = y - mLastTouchY

                if (!mIsDragging && !isInScale) {
                    // Use Pythagoras to see if drag length is larger than
                    // touch slop
                    val piphogor = sqrt(dx * dx + dy * dy)
                    mIsDragging = piphogor >= mTouchSlop
                }

                if (mIsDragging && !isInScale) {
                    if (iScaleImage.isCanScroll()) {
                        if (iScaleImage.isCanDragToDismiss() && abs(dy).toInt() > abs(dx).toInt()) {
                            iScaleImage.dragToDismiss(dy)
                            isNowDragToDismiss = true
                        } else {
                            if (!isNowDragToDismiss) iScaleImage.scrollImage(dx, dy)
                        }
                    } else {
                        if (iScaleImage.isCanDragToDismiss()) {
                            iScaleImage.dragToDismiss(dy)
                            isNowDragToDismiss = true
                        }
                    }

                    mLastTouchX = x
                    mLastTouchY = y

                    mVelocityTracker?.addMovement(ev)
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                mActivePointerId = INVALID_POINTER_ID
                // Recycle Velocity Tracker
                if (null != mVelocityTracker) {
                    mVelocityTracker?.recycle()
                    mVelocityTracker = null
                }
            }
            MotionEvent.ACTION_UP -> {
                mActivePointerId = INVALID_POINTER_ID
                var isFlingExec = false
                if (mIsDragging) {
                    if (mVelocityTracker != null) {
                        mLastTouchX = getActiveX(ev)
                        mLastTouchY = getActiveY(ev)

                        // Compute velocity within the last 1000ms
                        mVelocityTracker?.addMovement(ev)
                        mVelocityTracker?.computeCurrentVelocity(1000)

                        val vX = mVelocityTracker?.xVelocity
                        val vY = mVelocityTracker?.yVelocity

                        // If the velocity is greater than minVelocity, call
                        // listener
                        if (vX != null && vY != null && Math.max(Math.abs(vX), Math.abs(vY)) >= mMinimumVelocity) {
                            isFlingExec = true
                            iScaleImage.fling(
                                mLastTouchX.toInt(), mLastTouchY.toInt(), -vX.toInt(),
                                -vY.toInt(), isNowDragToDismiss
                            )
                        }
                    }
                }

                releaseDragTouch(isFlingExec)

                // Recycle Velocity Tracker
                mVelocityTracker?.let {
                    mVelocityTracker?.recycle()
                    mVelocityTracker = null
                }
            }
            MotionEvent.ACTION_POINTER_UP -> {
                val pointerIndex = getPointerIndex(ev.action)
                val pointerId = ev.getPointerId(pointerIndex)
                if (pointerId == mActivePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    val newPointerIndex = if (pointerIndex == 0) 1 else 0
                    mActivePointerId = ev.getPointerId(newPointerIndex)
                    mLastTouchX = ev.getX(newPointerIndex)
                    mLastTouchY = ev.getY(newPointerIndex)
                }
            }
        }

        mActivePointerIndex = ev
            .findPointerIndex(
                if (mActivePointerId != INVALID_POINTER_ID)
                    mActivePointerId
                else
                    0
            )
        return true
    }

    private fun releaseDragTouch(isFlingExec: Boolean) {
        if (!isInScale && mIsDragging) {
            if (iScaleImage.isCanDragToDismiss() && !isFlingExec) {
                iScaleImage.toDefaultPosition()
            }
        }
        if (isInScale) {
            isInScale = false
            iScaleImage.endOfGestureZoom()
        }
    }

    private fun getPointerIndex(action: Int): Int {
        return action and MotionEvent.ACTION_POINTER_INDEX_MASK shr MotionEvent.ACTION_POINTER_INDEX_SHIFT
    }

}