package com.example.tspayscreentest

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import android.view.Gravity

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val textView = TextView(this).apply {
            text = "TSPAY Screen Test"
            textSize = 24f
            gravity = Gravity.CENTER
        }

        setContentView(textView)
    }
}
