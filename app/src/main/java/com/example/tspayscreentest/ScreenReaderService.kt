
package com.example.tspayscreentest

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.util.Log

class ScreenReaderService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("TSPAY", "Screen Reader Connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val root = rootInActiveWindow ?: return
        val screenText = StringBuilder()

        readScreen(root, screenText)
        root.recycle()

        Log.d("TSPAY", "Screen text: $screenText")
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
            readScreen(child, result)
            child.recycle()
        }
    }

    override fun onInterrupt() {
        Log.d("TSPAY", "Screen Reader Interrupted")
    }
}
