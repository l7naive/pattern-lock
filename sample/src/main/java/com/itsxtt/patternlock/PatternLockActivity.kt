package com.itsxtt.patternlock

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_pattern_9x9.*
import kotlinx.android.synthetic.main.activity_pattern_default.*
import kotlinx.android.synthetic.main.activity_pattern_jd.*
import kotlinx.android.synthetic.main.activity_pattern_with_indicator.*
import java.util.ArrayList


class PatternLockActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var type = intent.getIntExtra(MainActivity.KEY_PATTERN_TYPE, MainActivity.TYPE_DEFAULT)
        when(type) {
            MainActivity.TYPE_DEFAULT -> {
                setContentView(R.layout.activity_pattern_default)
                defaultPatternLockView.setOnPatternListener(listener)
            }
            MainActivity.TYPE_JD_STYLE -> {
                setContentView(R.layout.activity_pattern_jd)
                jdPatternLockView.setOnPatternListener(listener)
            }
            MainActivity.TYPE_WITH_INDICATOR -> {
                setContentView(R.layout.activity_pattern_with_indicator)
                indicatorPatternLockView.setOnPatternListener(listener)
            }
            MainActivity.TYPE_9x9 -> {
                setContentView(R.layout.activity_pattern_9x9)
                ninePatternLockView.setOnPatternListener(listener)
            }
            MainActivity.TYPE_SECURE_MODE -> {
                setContentView(R.layout.activity_pattern_default)
                defaultPatternLockView.enableSecureMode()
                defaultPatternLockView.setOnPatternListener(listener)
            }
        }

    }

    private var listener  = object : PatternLockView.OnPatternListener {

        override fun onStarted() {
            super.onStarted()
        }

        override fun onProgress(ids: ArrayList<Int>) {
            super.onProgress(ids)
        }

        override fun onComplete(ids: ArrayList<Int>): Boolean {
            var isCorrect = TextUtils.equals("012", getPatternString(ids))
            var tip: String
            if (isCorrect) {
                tip = "correct:" + getPatternString(ids)
            } else {
                tip = "error:" + getPatternString(ids)
            }
            Toast.makeText(this@PatternLockActivity, tip, Toast.LENGTH_SHORT).show()
            return isCorrect
        }
    }

    private fun getPatternString(ids: ArrayList<Int>) : String {
        var result: String = ""
        for (id in ids) {
            result += id.toString()
        }
        return result
    }
}