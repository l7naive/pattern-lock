package com.itsxtt.patternlock

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View

//TODO: internal has no effect, still accessible outside the module
internal class Cell(context: Context,
                    var index: Int,
                    private var regularCellBackground: Drawable?,
                    private var regularDotColor: Int,
                    private var regularDotRadiusRatio: Float,
                    private var selectedCellBackground: Drawable?,
                    private var selectedDotColor: Int,
                    private var selectedDotRadiusRatio: Float,
                    private var errorCellBackground: Drawable?,
                    private var errorDotColor: Int,
                    private var errorDotRadiusRatio: Float,
                    private var lineStyle: Int,
                    private var regularLineColor: Int,
                    private var errorLineColor: Int,
                    private var columnCount: Int) : View(context) {

    private var currentState: State = State.REGULAR
    private var paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var cellWidth = MeasureSpec.getSize(widthMeasureSpec) / columnCount
        var cellHeight = cellWidth
        setMeasuredDimension(cellWidth, cellHeight)
    }

    override fun onDraw(canvas: Canvas?) {
        logg("Cell=>onDraw")
        when(currentState) {
            State.REGULAR -> drawDot(canvas, regularCellBackground, regularDotColor, regularDotRadiusRatio)
            State.SELECTED -> drawDot(canvas, selectedCellBackground, selectedDotColor, selectedDotRadiusRatio)
            State.ERROR -> drawDot(canvas, errorCellBackground, errorDotColor, errorDotRadiusRatio)
        }
    }

    fun logg(log: String) {
        Log.d("plv_", log)
    }

    private fun drawDot(canvas: Canvas?,
                        background: Drawable?,
                        dotColor: Int,
                        radiusRation: Float) {
        var radius = (Math.min(width, height) - (paddingLeft + paddingRight)) / 2
        var centerX = width / 2
        var centerY = height / 2

        if (background is ColorDrawable) {
            paint.color = background.color
            paint.style = Paint.Style.FILL
            canvas?.drawCircle(centerX.toFloat(), centerY.toFloat(), radius.toFloat(), paint)
        } else {
            background?.setBounds(paddingLeft, paddingTop, width - paddingRight, height - paddingBottom)
            background?.draw(canvas)
        }

        paint.color = dotColor
        paint.style = Paint.Style.FILL
        canvas?.drawCircle(centerX.toFloat(), centerY.toFloat(), radius * radiusRation, paint)
    }


    fun getCenter() : Point {
        var point = Point()
        point.x = left + (right - left) / 2
        point.y = top + (bottom - top) / 2
        return point
    }

    fun setState(state: State) {
        currentState = state
        invalidate()
    }

 }