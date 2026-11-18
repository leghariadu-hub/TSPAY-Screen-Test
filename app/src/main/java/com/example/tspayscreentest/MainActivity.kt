
package com.example.tspayscreentest

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
        }

        val title = TextView(this).apply {
            text = "TSPAY Screen Reader"
            textSize = 24f
            gravity = Gravity.CENTER
        }

        val status = TextView(this).apply {
            text = "Screen Reading Setup"
            textSize = 16f
            gravity = Gravity.CENTER
        }

        val permissionButton = Button(this).apply {
            text = "Open Accessibility Settings"

            setOnClickListener {
                startActivity(
                    Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                )
            }
        }

        layout.addView(title)
        layout.addView(status)
        layout.addView(permissionButton)

        setContentView(layout)
    }
}
