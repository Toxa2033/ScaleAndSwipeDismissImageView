package ru.test.library

internal interface IScaleImage {
    fun scaleImage(scaleFactor:Float,x:Float=0f,y:Float=0f): Boolean
    fun scaleByDoubleTap(scale:Float, x:Float=0f, y:Float=0f):Boolean
    fun scrollImage(dx:Float, dy:Float)
    fun dragToDismiss(dy:Float)
    fun fling(lastX:Int,lastY:Int,vX:Int,vY:Int,isDragToDismiss:Boolean=false)
    fun cancelFling()
    fun getScale():Float
    fun toDefaultPosition()

    fun getMaxScale():Float
    fun getMinScale():Float
    fun getMidScale():Float

    fun isCanScroll():Boolean
    fun isCanDragToDismiss():Boolean

    fun endOfGestureZoom()
}