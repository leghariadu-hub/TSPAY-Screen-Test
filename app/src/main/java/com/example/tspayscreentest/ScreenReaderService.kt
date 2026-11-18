
package com.example.tspayscreentest

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.util.Log
import kotlin.math.pow
import kotlin.math.floor

class ScreenReaderService : AccessibilityService() {

    private val prefs by lazy {
        getSharedPreferences("TSPAY_SETTINGS", MODE_PRIVATE)
    }

    private val minimumAmount = 500.0

    private val orderPattern = Regex(
        """Amount\s*:\s*(?:PKR|Rs\.?|₨)\s*([\d,]+(?:\.\d{1,2})?)""",
        RegexOption.IGNORE_CASE
    )

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("TSPAY", "Screen Reader Connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (!prefs.getBoolean("enabled", false)) return

        val selectedAmount = prefs.getInt("max_amount", 5000)
        if (selectedAmount < minimumAmount) return

        val upperLimit = calculateUpperLimit(selectedAmount)

        val root = rootInActiveWindow ?: return

        try {
            val screenText = StringBuilder()
            readScreen(root, screenText)

            val matches = orderPattern.findAll(screenText.toString())

            val validAmounts = matches.mapNotNull { match ->
                match.groupValues[1]
                    .replace(",", "")
                    .toDoubleOrNull()
            }.filter { amount ->
                amount >= selectedAmount &&
                amount < upperLimit
            }.toList()

            if (validAmounts.isNotEmpty()) {
                Log.d(
                    "TSPAY",
                    "Matching order amounts: $validAmounts"
                )
            }

        } catch (e: Exception) {
            Log.e("TSPAY", "Screen reading error", e)
        } finally {
            root.recycle()
        }
    }

    private fun calculateUpperLimit(amount: Int): Double {
        val step = when {
            amount < 1000 -> 100.0
            amount < 10000 -> 1000.0
            else -> 1000.0
        }

        return (floor(amount / step) + 1) * step
    }

    private fun readScreen(
        node: AccessibilityNodeInfo,
        result: StringBuilder
    ) {
        node.text?.let {
            result.append(it).append("\n")
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            try {
                readScreen(child, result)
            } finally {
                child.recycle()
            }
        }
    }

    override fun onInterrupt() {
        Log.d("TSPAY", "Screen Reader Interrupted")
    }
}
