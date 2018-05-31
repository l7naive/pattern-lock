package com.itsxtt.patternlocksample

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.itsxtt.patternlocksample.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val KEY_PATTERN_TYPE = "type"

        const val TYPE_DEFAULT = 0
        const val TYPE_WITH_INDICATOR = 1
        const val TYPE_JD_STYLE = 2
        const val TYPE_9x9 = 3
        const val TYPE_SECURE_MODE = 4
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        defaultBtn.setOnClickListener { _-> startPatternActivity(TYPE_DEFAULT) }
        jdStyleBtn.setOnClickListener { _-> startPatternActivity(TYPE_JD_STYLE) }
        indicatorBtn.setOnClickListener { _-> startPatternActivity(TYPE_WITH_INDICATOR) }
        nineBtn.setOnClickListener { _-> startPatternActivity(TYPE_9x9) }
        secureModeBtn.setOnClickListener { _-> startPatternActivity(TYPE_SECURE_MODE) }
    }

    private fun startPatternActivity(type: Int) {
        val intent = Intent(this, PatternLockActivity::class.java)
        intent.putExtra(KEY_PATTERN_TYPE, type)
        startActivity(intent)
    }
}
