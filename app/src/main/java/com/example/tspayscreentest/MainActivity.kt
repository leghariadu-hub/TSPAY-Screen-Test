package com.example.tspayscreentest

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.provider.Settings
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import android.text.InputType

class MainActivity : Activity() {

    private val prefs by lazy {
        getSharedPreferences("TSPAY_SETTINGS", MODE_PRIVATE)
    }

    private lateinit var statusText: TextView
    private lateinit var maxAmountInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(40, 50, 40, 30)
            setBackgroundColor(Color.WHITE)
        }

        val title = TextView(this).apply {
            text = "TSPAY AUTO ORDER"
            textSize = 26f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
        }

        val minimum = TextView(this).apply {
            text = "Minimum Amount: Rs. 500"
            textSize = 18f
            setTextColor(Color.BLACK)
        }

        val maximum = TextView(this).apply {
            text = "Set Maximum Amount (PKR)"
            textSize = 18f
            setTextColor(Color.BLACK)
        }

        maxAmountInput = EditText(this).apply {
            hint = "Enter maximum amount"
            inputType = InputType.TYPE_CLASS_NUMBER
            setSingleLine(true)
            setText(prefs.getInt("max_amount", 5000).toString())
        }

        statusText = TextView(this).apply {
            text = if (prefs.getBoolean("enabled", false))
                "Status: STARTED"
            else
                "Status: STOPPED"
            textSize = 20f
            gravity = Gravity.CENTER
            setTextColor(Color.BLACK)
        }

        val saveButton = Button(this).apply {
            text = "SAVE AMOUNT"

            setOnClickListener {
                val amount = maxAmountInput.text.toString().toIntOrNull()

                if (amount == null || amount < 500) {
                    Toast.makeText(
                        this@MainActivity,
                        "Maximum must be 500 or higher",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    prefs.edit()
                        .putInt("max_amount", amount)
                        .apply()

                    Toast.makeText(
                        this@MainActivity,
                        "Amount Saved",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        val startButton = Button(this).apply {
            text = "START"

            setOnClickListener {
                prefs.edit()
                    .putBoolean("enabled", true)
                    .apply()

                statusText.text = "Status: STARTED"

                Toast.makeText(
                    this@MainActivity,
                    "Settings enabled",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        val stopButton = Button(this).apply {
            text = "STOP"

            setOnClickListener {
                prefs.edit()
                    .putBoolean("enabled", false)
                    .apply()

                statusText.text = "Status: STOPPED"

                Toast.makeText(
                    this@MainActivity,
                    "Settings disabled",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        val accessibilityButton = Button(this).apply {
            text = "ACCESSIBILITY SETTINGS"

            setOnClickListener {
                startActivity(
                    Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                )
            }
        }

        val note = TextView(this).apply {
            text = "Order range: Rs. 500 to your saved maximum"
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(Color.DKGRAY)
        }

        fun addItem(view: android.view.View) {
            layout.addView(
                view,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }

        addItem(title)
        addItem(minimum)
        addItem(maximum)
        addItem(maxAmountInput)
        addItem(saveButton)
        addItem(statusText)
        addItem(startButton)
        addItem(stopButton)
        addItem(accessibilityButton)
        addItem(note)

        setContentView(layout)
    }
}
