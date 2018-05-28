package com.itsxtt.patternlock

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.GridLayout
import java.util.*


class PatternLockView : GridLayout {

    companion object {
        const val DEFAULT_RADIUS_RATIO = 0.3f
        const val DEFAULT_LINE_WIDTH = 4f // unit: dp
        const val DEFAULT_SPACING = 8f // unit: dp
        const val DEFAULT_ROW_COUNT = 3
        const val DEFAULT_COLUMN_COUNT = 3
    }

    private var regularCellBackground: Drawable? = null
    private var regularDotColor: Int = 0
    private var regularDotRadiusRatio: Float = 0f

    private var selectedCellBackground: Drawable? = null
    private var selectedDotColor: Int = 0
    private var selectedDotRadiusRatio: Float = 0f

    private var errorCellBackground: Drawable? = null
    private var errorDotColor: Int = 0
    private var errorDotRadiusRatio: Float = 0f

    /**
     * determine the line's style
     * common: 1
     * with indicator: 2
     */
    private var lineStyle: Int = 0

    private var lineWidth: Int = 0
    private var regularLineColor: Int = 0
    private var errorLineColor: Int = 0

    private var spacing: Int = 0

    private var plvRowCount: Int = 0
    private var plvColumnCount: Int = 0

    private var cells: ArrayList<Cell> = ArrayList()
    private var selectedCells: ArrayList<Cell> = ArrayList()

    private var linePaint: Paint = Paint()
    private var linePath: Path = Path()

    private var lastX: Float = 0f
    private var lastY: Float = 0f

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        var ta = context.obtainStyledAttributes(attributeSet, R.styleable.PatternLockView)
        regularCellBackground = ta.getDrawable(R.styleable.PatternLockView_plv_regularCellBackground)
        regularDotColor = ta.getColor(R.styleable.PatternLockView_plv_regularDotColor, Color.GRAY)
        regularDotRadiusRatio = ta.getFloat(R.styleable.PatternLockView_plv_regularDotRadiusRatio, DEFAULT_RADIUS_RATIO)

        selectedCellBackground = ta.getDrawable(R.styleable.PatternLockView_plv_selectedCellBackground)
        selectedDotColor = ta.getColor(R.styleable.PatternLockView_plv_selectedDotColor, Color.BLUE)
        selectedDotRadiusRatio = ta.getFloat(R.styleable.PatternLockView_plv_selectedDotRadiusRatio, DEFAULT_RADIUS_RATIO)

        errorCellBackground = ta.getDrawable(R.styleable.PatternLockView_plv_errorCellBackground)
        errorDotColor = ta.getColor(R.styleable.PatternLockView_plv_errorDotColor, Color.RED)
        errorDotRadiusRatio = ta.getFloat(R.styleable.PatternLockView_plv_errorDotRadiusRatio, DEFAULT_RADIUS_RATIO)

        lineStyle = ta.getInt(R.styleable.PatternLockView_plv_lineStyle, 1)
        lineWidth = ta.getDimensionPixelSize(R.styleable.PatternLockView_plv_lineWidth,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_LINE_WIDTH, context.resources.displayMetrics).toInt())
        regularLineColor = ta.getColor(R.styleable.PatternLockView_plv_regularLineColor, Color.BLUE)
        errorLineColor = ta.getColor(R.styleable.PatternLockView_plv_errorLineColor, Color.RED)

        spacing = ta.getDimensionPixelSize(R.styleable.PatternLockView_plv_spacing,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_SPACING, context.resources.displayMetrics).toInt())

        plvRowCount = ta.getInteger(R.styleable.PatternLockView_plv_rowCount, DEFAULT_ROW_COUNT)
        plvColumnCount = ta.getInteger(R.styleable.PatternLockView_plv_columnCount, DEFAULT_COLUMN_COUNT)

        ta.recycle()

        rowCount = plvRowCount
        columnCount = plvColumnCount

        setupCells()
        initPathPaint()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when(event?.action) {
            MotionEvent.ACTION_DOWN -> {
                var hitCell = getHitCell(event.x.toInt(), event.y.toInt())
                if (hitCell == null) {
                    return false
                } else {
                    notifyCellSelected(hitCell)
                }
            }
            MotionEvent.ACTION_MOVE -> handleActionMove(event)

            MotionEvent.ACTION_UP -> reset()

            MotionEvent.ACTION_CANCEL -> reset()

            else -> return false
        }
        return true
    }

    private fun handleActionMove(event: MotionEvent) {
        var hitCell = getHitCell(event.x.toInt(), event.y.toInt())
        if (hitCell != null) {
            if (!selectedCells.contains(hitCell)) {
                notifyCellSelected(hitCell)
            }
        }

        for(i in 0..(selectedCells.size - 1)) {
            var center = selectedCells[i].getCenter()
        }

        lastX = event.x
        lastY = event.y

        invalidate()
    }

    private fun notifyCellSelected(cell: Cell) {
        cell.setState(State.SELECTED)
        selectedCells.add(cell)

        var point = cell.getCenter()
        if (selectedCells.size == 1) {
            linePath.moveTo(point.x.toFloat(), point.y.toFloat())
            logg("moveTo")
        } else {
            linePath.lineTo(point.x.toFloat(), point.y.toFloat())
            logg("lineTo")
        }
    }

    override fun dispatchDraw(canvas: Canvas?) {
        super.dispatchDraw(canvas)
        logg("dispatch")
        canvas?.drawPath(linePath, linePaint)

        if (selectedCells.size > 0 && lastX > 0 && lastY > 0) {
            var center = selectedCells[selectedCells.size - 1].getCenter()
            canvas?.drawLine(center.x.toFloat(), center.y.toFloat(), lastX, lastY, linePaint)
        }
    }

    private fun setupCells() {
        for(i in 0..(plvRowCount-1)) {
            for(j in 0..(plvColumnCount-1)) {
                var cell = Cell(context, i * plvColumnCount + j,
                        regularCellBackground, regularDotColor, regularDotRadiusRatio,
                        selectedCellBackground, selectedDotColor, selectedDotRadiusRatio,
                        errorCellBackground, errorDotColor, errorDotRadiusRatio,
                        lineStyle, regularLineColor, errorLineColor, plvColumnCount)
                var cellPadding = spacing / 2
                cell.setPadding(cellPadding, cellPadding, cellPadding, cellPadding)
                addView(cell)

                cells.add(cell)
            }
        }
    }

    private fun initPathPaint() {
        linePaint.isAntiAlias = true
        linePaint.isDither = true
        linePaint.style = Paint.Style.STROKE
        linePaint.strokeJoin = Paint.Join.ROUND
        linePaint.strokeCap = Paint.Cap.ROUND

        linePaint.strokeWidth = lineWidth.toFloat()
        linePaint.color = regularLineColor
    }

    private fun reset() {
        logg(selectedCells.size.toString())
        for(cell in selectedCells) {
            cell.setState(State.REGULAR)
        }
        selectedCells.clear()
        linePaint.color = regularLineColor
        linePath.reset()

        invalidate()
    }

    private fun getHitCell(x: Int, y: Int) : Cell? {
        for(cell in cells) {
            if (isPointInsideView(cell, x, y)) {
                return cell
            }
        }
        return null
    }

    private fun isPointInsideView(view: View, x: Int, y: Int) : Boolean {
        var insidePadding = view.width * 0.2
        return x >= view.left + insidePadding &&
                x <= view.right - insidePadding &&
                y >= view.top + insidePadding &&
                y <= view.bottom - insidePadding
    }
    
    fun logg(log: String) {
        Log.d("plv_", log)
    }
}