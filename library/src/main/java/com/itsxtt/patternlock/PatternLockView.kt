package com.itsxtt.patternlock

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.GridLayout
import java.util.*


class PatternLockView : GridLayout {

    companion object {
        const val DEFAULT_RADIUS_RATIO = 0.3f
        const val DEFAULT_LINE_WIDTH = 2f // unit: dp
        const val DEFAULT_SPACING = 24f // unit: dp
        const val DEFAULT_ROW_COUNT = 3
        const val DEFAULT_COLUMN_COUNT = 3
        const val DEFAULT_ERROR_DURATION = 400 // unit: ms
        const val DEFAULT_HIT_AREA_PADDING_RATIO = 0.2f
        const val DEFAULT_INDICATOR_SIZE_RATIO = 0.2f

        const val LINE_STYLE_COMMON = 1
        const val LINE_STYLE_INDICATOR = 2
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
     * invisible: 3
     */
    private var lineStyle: Int = 0

    private var lineWidth: Int = 0
    private var regularLineColor: Int = 0
    private var errorLineColor: Int = 0

    private var spacing: Int = 0

    private var plvRowCount: Int = 0
    private var plvColumnCount: Int = 0

    private var errorDuration: Int = 0
    private var hitAreaPaddingRatio: Float = 0f
    private var indicatorSizeRatio: Float = 0f

    private var cells: ArrayList<Cell> = ArrayList()
    private var selectedCells: ArrayList<Cell> = ArrayList()

    private var linePaint: Paint = Paint()
    private var linePath: Path = Path()

    private var lastX: Float = 0f
    private var lastY: Float = 0f

    private var isSecureMode = false

    private var onPatternListener: OnPatternListener? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        var ta = context.obtainStyledAttributes(attributeSet, R.styleable.PatternLockView)
        regularCellBackground = ta.getDrawable(R.styleable.PatternLockView_plv_regularCellBackground)
        regularDotColor = ta.getColor(R.styleable.PatternLockView_plv_regularDotColor, ContextCompat.getColor(context, R.color.regularColor))
        regularDotRadiusRatio = ta.getFloat(R.styleable.PatternLockView_plv_regularDotRadiusRatio, DEFAULT_RADIUS_RATIO)

        selectedCellBackground = ta.getDrawable(R.styleable.PatternLockView_plv_selectedCellBackground)
        selectedDotColor = ta.getColor(R.styleable.PatternLockView_plv_selectedDotColor, ContextCompat.getColor(context, R.color.selectedColor))
        selectedDotRadiusRatio = ta.getFloat(R.styleable.PatternLockView_plv_selectedDotRadiusRatio, DEFAULT_RADIUS_RATIO)

        errorCellBackground = ta.getDrawable(R.styleable.PatternLockView_plv_errorCellBackground)
        errorDotColor = ta.getColor(R.styleable.PatternLockView_plv_errorDotColor, ContextCompat.getColor(context, R.color.errorColor))
        errorDotRadiusRatio = ta.getFloat(R.styleable.PatternLockView_plv_errorDotRadiusRatio, DEFAULT_RADIUS_RATIO)

        lineStyle = ta.getInt(R.styleable.PatternLockView_plv_lineStyle, 1)
        lineWidth = ta.getDimensionPixelSize(R.styleable.PatternLockView_plv_lineWidth,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_LINE_WIDTH, context.resources.displayMetrics).toInt())
        regularLineColor = ta.getColor(R.styleable.PatternLockView_plv_regularLineColor, ContextCompat.getColor(context, R.color.selectedColor))
        errorLineColor = ta.getColor(R.styleable.PatternLockView_plv_errorLineColor, ContextCompat.getColor(context, R.color.errorColor))

        spacing = ta.getDimensionPixelSize(R.styleable.PatternLockView_plv_spacing,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_SPACING, context.resources.displayMetrics).toInt())

        plvRowCount = ta.getInteger(R.styleable.PatternLockView_plv_rowCount, DEFAULT_ROW_COUNT)
        plvColumnCount = ta.getInteger(R.styleable.PatternLockView_plv_columnCount, DEFAULT_COLUMN_COUNT)

        errorDuration = ta.getInteger(R.styleable.PatternLockView_plv_errorDuration, DEFAULT_ERROR_DURATION)
        hitAreaPaddingRatio = ta.getFloat(R.styleable.PatternLockView_plv_hitAreaPaddingRatio, DEFAULT_HIT_AREA_PADDING_RATIO)
        indicatorSizeRatio = ta.getFloat(R.styleable.PatternLockView_plv_indicatorSizeRatio, DEFAULT_INDICATOR_SIZE_RATIO)

        ta.recycle()

        rowCount = plvRowCount
        columnCount = plvColumnCount

        setupCells()
        initPathPaint()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when(event?.action) {
            MotionEvent.ACTION_DOWN -> {
                // Calling "reset()" here fixes the problem of reconnecting the dots when user set pattern fast
                // For example : If we set the limit that "connect at least four dots", and when we connect 3 dots and lift the finger,
                // within second if we select a dot it will consider 4 dots. Previously 3 and 1 after lifting the finger. So, calling "reset()" here will
                // fix the problem of reconnecting the dots.
                reset()
                val hitCell = getHitCell(event.x.toInt(), event.y.toInt())
                if (hitCell == null) {
                    return false
                } else {
                    onPatternListener?.onStarted()
                    notifyCellSelected(hitCell)
                }
            }
            MotionEvent.ACTION_MOVE -> handleActionMove(event)

            MotionEvent.ACTION_UP -> onFinish()

            MotionEvent.ACTION_CANCEL -> reset()

            else -> return false
        }
        return true
    }

    private fun handleActionMove(event: MotionEvent) {
        var hitCell = getHitCell(event.x.toInt(), event.y.toInt())
        if (hitCell != null) {
            // These check fixes the problem of missing middle dot between two dots.
            if ((selectedCells.last().index == 0 && hitCell.index == 6)) {
                if (!selectedCells.contains(cells[3])) {
                    notifyCellSelected(cells[3])
                }
            } else if ((selectedCells.last().index == 0 && hitCell.index == 2)) {
                if (!selectedCells.contains(cells[1])) {
                    notifyCellSelected(cells[1])
                }
            } else if ((selectedCells.last().index == 2 && hitCell.index == 0)) {
                if (!selectedCells.contains(cells[1])) {
                    notifyCellSelected(cells[1])
                }
            } else if ((selectedCells.last().index == 2 && hitCell.index == 8)) {
                if (!selectedCells.contains(cells[5])) {
                    notifyCellSelected(cells[5])
                }
            } else if ((selectedCells.last().index == 6 && hitCell.index == 0)) {
                if (!selectedCells.contains(cells[3])) {
                    notifyCellSelected(cells[3])
                }
            } else if ((selectedCells.last().index == 6 && hitCell.index == 8)) {
                if (!selectedCells.contains(cells[7])) {
                    notifyCellSelected(cells[7])
                }
            } else if ((selectedCells.last().index == 8 && hitCell.index == 2)) {
                if (!selectedCells.contains(cells[5])) {
                    notifyCellSelected(cells[5])
                }
            } else if ((selectedCells.last().index == 8 && hitCell.index == 6)) {
                if (!selectedCells.contains(cells[7])) {
                    notifyCellSelected(cells[7])
                }
            } else if (
                (selectedCells.last().index == 5 && hitCell.index == 3) ||
                (selectedCells.last().index == 3 && hitCell.index == 5) ||
                (selectedCells.last().index == 1 && hitCell.index == 7) ||
                (selectedCells.last().index == 7 && hitCell.index == 1)
            ) {
                if (!selectedCells.contains(cells[4])) {
                    notifyCellSelected(cells[4])
                }
            }
            if (!selectedCells.contains(hitCell)) {
                notifyCellSelected(hitCell)
            }
        }

        lastX = event.x
        lastY = event.y

        invalidate()
    }

    private fun notifyCellSelected(cell: Cell) {
        selectedCells.add(cell)
        onPatternListener?.onProgress(generateSelectedIds())
        if (isSecureMode) return
        cell.setState(State.SELECTED)
        var center = cell.getCenter()
        if (selectedCells.size == 1) {
            if (lineStyle == LINE_STYLE_COMMON) {
                linePath.moveTo(center.x.toFloat(), center.y.toFloat())
            }
        } else {
            if (lineStyle == LINE_STYLE_COMMON) {
                linePath.lineTo(center.x.toFloat(), center.y.toFloat())
            } else if (lineStyle == LINE_STYLE_INDICATOR) {
                var previousCell = selectedCells[selectedCells.size - 2]
                var previousCellCenter = previousCell.getCenter()
                var diffX = center.x - previousCellCenter.x
                var diffY = center.y - previousCellCenter.y
                var radius = cell.getRadius()
                var length = Math.sqrt((diffX * diffX + diffY * diffY).toDouble())

                linePath.moveTo((previousCellCenter.x + radius * diffX / length).toFloat(), (previousCellCenter.y + radius * diffY / length).toFloat())
                linePath.lineTo((center.x - radius * diffX / length).toFloat(), (center.y - radius * diffY / length).toFloat())

                val degree = Math.toDegrees(Math.atan2(diffY.toDouble(), diffX.toDouble())) + 90
                previousCell.setDegree(degree.toFloat())
                previousCell.invalidate()
            }
        }
    }

    override fun dispatchDraw(canvas: Canvas?) {
        super.dispatchDraw(canvas)
        if (isSecureMode) return
        canvas?.drawPath(linePath, linePaint)
        if (selectedCells.size > 0 && lastX > 0 && lastY > 0) {
            if (lineStyle == LINE_STYLE_COMMON) {
                var center = selectedCells[selectedCells.size - 1].getCenter()
                canvas?.drawLine(center.x.toFloat(), center.y.toFloat(), lastX, lastY, linePaint)
            } else if (lineStyle == LINE_STYLE_INDICATOR) {
                var lastCell = selectedCells[selectedCells.size - 1]
                var lastCellCenter = lastCell.getCenter()
                var radius = lastCell.getRadius()

                if (!(lastX >= lastCellCenter.x - radius &&
                        lastX <= lastCellCenter.x + radius &&
                        lastY >= lastCellCenter.y - radius &&
                        lastY <= lastCellCenter.y + radius)) {
                    var diffX = lastX - lastCellCenter.x
                    var diffY = lastY - lastCellCenter.y
                    var length = Math.sqrt((diffX * diffX + diffY * diffY).toDouble())
                    canvas?.drawLine((lastCellCenter.x + radius * diffX / length).toFloat(),
                            (lastCellCenter.y + radius * diffY / length).toFloat(),
                            lastX, lastY, linePaint)
                }
            }
        }

    }

    private fun setupCells() {
        for(i in 0..(plvRowCount-1)) {
            for(j in 0..(plvColumnCount-1)) {
                var cell = Cell(context, i * plvColumnCount + j,
                        regularCellBackground, regularDotColor, regularDotRadiusRatio,
                        selectedCellBackground, selectedDotColor, selectedDotRadiusRatio,
                        errorCellBackground, errorDotColor, errorDotRadiusRatio,
                        lineStyle, regularLineColor, errorLineColor, plvColumnCount, indicatorSizeRatio)
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
        for(cell in selectedCells) {
            cell.reset()
        }
        selectedCells.clear()
        linePaint.color = regularLineColor
        linePath.reset()

        lastX = 0f
        lastY = 0f

        invalidate()
    }

    fun enableSecureMode() {
        isSecureMode = true
    }

    fun disableSecureMode() {
        isSecureMode = false
    }

    private fun getHitCell(x: Int, y: Int) : Cell? {
        for(cell in cells) {
            if (isSelected(cell, x, y)) {
                return cell
            }
        }
        return null
    }

    private fun isSelected(view: View, x: Int, y: Int) : Boolean {
        var innerPadding = view.width * hitAreaPaddingRatio
        return x >= view.left + innerPadding &&
                x <= view.right - innerPadding &&
                y >= view.top + innerPadding &&
                y <= view.bottom - innerPadding
    }

    private fun onFinish() {
        lastX = 0f
        lastY = 0f

        var isCorrect = onPatternListener?.onComplete(generateSelectedIds())
        if (isCorrect != null && isCorrect) {
            reset()
        } else {
            onError()
        }
    }

    private fun generateSelectedIds() : ArrayList<Int> {
        var result = ArrayList<Int>()
        for(cell in selectedCells) {
            result.add(cell.index)
        }
        return result
    }

    private fun onError() {
        if (isSecureMode) {
            reset()
            return
        }
        for (cell in selectedCells) {
            cell.setState(State.ERROR)
        }
        linePaint.color = errorLineColor
        invalidate()

        postDelayed({
            reset()
        }, errorDuration.toLong())

    }

    fun setOnPatternListener(listener: OnPatternListener) {
        onPatternListener = listener
    }

    interface OnPatternListener {
        fun onStarted(){}
        fun onProgress(ids: ArrayList<Int>){}
        fun onComplete(ids: ArrayList<Int>) : Boolean
    }
}