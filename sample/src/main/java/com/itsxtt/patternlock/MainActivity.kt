package com.itsxtt.patternlock

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_pattern_jd.*

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
        setContentView(R.layout.activity_pattern_default)
    }

    private fun startPatternActivity(type: Int) {
        val intent = Intent(this, PatternLockActivity::class.java)
        intent.putExtra(KEY_PATTERN_TYPE, type)
        startActivity(intent)
    }
}
