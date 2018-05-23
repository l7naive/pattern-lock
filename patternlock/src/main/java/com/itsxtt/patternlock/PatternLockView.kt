package com.itsxtt.patternlock

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import java.util.*


class PatternLockView : LinearLayout {

    companion object {
        const val DEFAULT_RADIUS_RATIO = 0.3f
        const val DEFAULT_LINE_WIDTH = 2f // unit: dp
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
     * none: 2
     * with indicator: 3
     */
    private var lineStyle: Int = 0

    private var lineWidth: Int = 0
    private var regularLineColor: Int = 0
    private var errorLineColor: Int = 0

    private var horizontalSpacing: Int = 0
    private var verticalSpacing: Int = 0

    private var rowCount = DEFAULT_ROW_COUNT
    private var columnCount = DEFAULT_COLUMN_COUNT

    constructor(context: Context) : super(context) {

    }

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

        horizontalSpacing = ta.getDimensionPixelSize(R.styleable.PatternLockView_plv_horizontalSpacing,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_SPACING, context.resources.displayMetrics).toInt())
        verticalSpacing = ta.getDimensionPixelSize(R.styleable.PatternLockView_plv_verticalSpacing,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_SPACING, context.resources.displayMetrics).toInt())
        ta.recycle()

        var cell = Cell(context, 0, 0,
                regularCellBackground, regularDotColor, regularDotRadiusRatio,
                selectedCellBackground, selectedDotColor, selectedDotRadiusRatio,
                errorCellBackground, errorDotColor, errorDotRadiusRatio,
                lineStyle, regularLineColor, errorLineColor)
        var x: Int = 0
        addView(cell, LayoutParams(500, 500))
        cell.setOnClickListener{_->
            if (x % 3 == 0) {
                cell.setState(State.REGULAR)
            } else if (x % 3 == 1) {
                cell.setState(State.SELECTED)
            } else {
                cell.setState(State.ERROR)
            }
            x ++
        }
    }
}