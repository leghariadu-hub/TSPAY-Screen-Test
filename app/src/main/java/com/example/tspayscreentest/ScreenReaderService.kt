
package com.example.tspayscreentest

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.util.Log

class ScreenReaderService : AccessibilityService() {

    private val prefs by lazy {
        getSharedPreferences("TSPAY_SETTINGS", MODE_PRIVATE)
    }

    private val minimumAmount = 500

    private val amountPattern = Regex(
        """(?:Rs\.?|PKR|₨)\s*([\d,]+(?:\.\d{1,2})?)""",
        RegexOption.IGNORE_CASE
    )

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("TSPAY", "Screen Reader Connected")
    }

    override fun onAccessibilityEvent(
        event: AccessibilityEvent?
    ) {
        if (event == null) return

        if (!prefs.getBoolean("enabled", false)) {
            return
        }

        val maximumAmount = prefs.getInt(
            "max_amount", 5000
        )

        val root = rootInActiveWindow ?: return

        try {
            val screenText = StringBuilder()
            readScreen(root, screenText)

            val text = screenText.toString()

            val amounts = amountPattern.findAll(text)
                .mapNotNull { match ->
                    match.groupValues[1]
                        .replace(",", "")
                        .toDoubleOrNull()
                }
                .toList()

            val validAmounts = amounts.filter {
                it >= minimumAmount &&
                it <= maximumAmount
            }

            if (validAmounts.isNotEmpty()) {
                Log.d(
                    "TSPAY",
                    "Matching amounts: $validAmounts"
                )
            }
        } catch (e: Exception) {
            Log.e("TSPAY", "Screen reading error", e)
        } finally {
            root.recycle()
        }
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
