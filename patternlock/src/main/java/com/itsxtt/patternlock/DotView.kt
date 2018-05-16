package com.itsxtt.patternlock

import android.content.Context
import android.graphics.Canvas
import android.view.View

//TODO: internal has no effect, still accessible outside the module
internal class DotView : View {

    private lateinit var currentState: State

    constructor(context: Context) : super(context) {
        init()
    }

    private fun init() {
        currentState = State.UNSELECTED
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }

    fun setState(state: State) {
        currentState = state
        invalidate()
    }

}