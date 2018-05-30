package com.itsxtt.patternlock

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.View

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
                    private var columnCount: Int,
                    private var indicatorSizeRatio: Float) : View(context) {

    private var currentState: State = State.REGULAR
    private var paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var currentDegree: Float = -1f
    private var indicatorPath: Path = Path()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var cellWidth = MeasureSpec.getSize(widthMeasureSpec) / columnCount
        var cellHeight = cellWidth
        setMeasuredDimension(cellWidth, cellHeight)
    }

    override fun onDraw(canvas: Canvas?) {
        when(currentState) {
            State.REGULAR -> drawDot(canvas, regularCellBackground, regularDotColor, regularDotRadiusRatio)
            State.SELECTED -> drawDot(canvas, selectedCellBackground, selectedDotColor, selectedDotRadiusRatio)
            State.ERROR -> drawDot(canvas, errorCellBackground, errorDotColor, errorDotRadiusRatio)
        }
    }

    private fun drawDot(canvas: Canvas?,
                        background: Drawable?,
                        dotColor: Int,
                        radiusRation: Float) {
        var radius = getRadius()
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

        if (lineStyle == PatternLockView.LINE_STYLE_INDICATOR &&
                (currentState == State.SELECTED || currentState == State.ERROR)) {
            drawIndicator(canvas)
        }
    }

    private fun drawIndicator(canvas: Canvas?) {
        if (currentDegree != -1f) {
            if (indicatorPath.isEmpty) {
                indicatorPath.fillType = Path.FillType.WINDING
                val radius = getRadius()
                val height = radius * indicatorSizeRatio
                indicatorPath.moveTo((width / 2).toFloat() , radius * (1 - selectedDotRadiusRatio - indicatorSizeRatio) / 2 + paddingTop)
                indicatorPath.lineTo((width /2).toFloat() - height, radius * (1 - selectedDotRadiusRatio - indicatorSizeRatio) / 2 + height + paddingTop)
                indicatorPath.lineTo((width / 2).toFloat() + height, radius * (1 - selectedDotRadiusRatio - indicatorSizeRatio) / 2 + height + paddingTop)
                indicatorPath.close()
            }

            if (currentState == State.SELECTED) {
                paint.color = regularLineColor
            } else {
                paint.color = errorLineColor
            }
            paint.style = Paint.Style.FILL

            canvas?.save()
            canvas?.rotate(currentDegree, (width / 2).toFloat(), (height / 2).toFloat())
            canvas?.drawPath(indicatorPath, paint)
            canvas?.restore()
        }
    }

    fun getRadius() : Int {
        return (Math.min(width, height) - (paddingLeft + paddingRight)) / 2
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

    fun setDegree(degree: Float) {
        currentDegree = degree
    }

    fun reset() {
        setState(State.REGULAR)
        currentDegree = -1f
    }

 }