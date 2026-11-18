
package com.example.tspayscreentest

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlin.math.floor

class ScreenReaderService : AccessibilityService() {

    private val prefs by lazy {
        getSharedPreferences("TSPAY_SETTINGS", MODE_PRIVATE)
    }

    private val handler = Handler(Looper.getMainLooper())
    private var busy = false
    private var lastActionTime = 0L

    private val amountRegex = Regex(
        """(?:PKR|Rs\.?|₨)\s*([\d,]+(?:\.\d{1,2})?)""",
        RegexOption.IGNORE_CASE
    )

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (!prefs.getBoolean("enabled", false)) return
        if (busy) return

        val now = System.currentTimeMillis()
        if (now - lastActionTime < 1200L) return

        val selected = prefs.getInt("max_amount", 5000)
        if (selected < 500) return

        val root = rootInActiveWindow ?: return

        busy = true
        try {
            val upper = upperLimit(selected)
            val found = findMatchingOrder(root, selected.toDouble(), upper)

            if (found) {
                lastActionTime = now
            } else {
                scrollDown(root)
                lastActionTime = now
            }
        } catch (e: Exception) {
            Log.e("TSPAY", "Automation error", e)
        } finally {
            busy = false
        }
    }

    private fun upperLimit(amount: Int): Double {
        val step = if (amount < 1000) 100.0 else 1000.0
        return (floor(amount / step) + 1) * step
    }

    private fun findMatchingOrder(
        root: AccessibilityNodeInfo,
        minimum: Double,
        maximum: Double
    ): Boolean {

        val nodes = mutableListOf<AccessibilityNodeInfo>()
        collectNodes(root, nodes)

        for (node in nodes) {
            val text = node.text?.toString()?.trim() ?: continue

            if (!text.startsWith("Amount", ignoreCase = true)) {
                continue
            }

            val match = amountRegex.find(text) ?: continue

            val amount = match.groupValues[1]
                .replace(",", "")
                .toDoubleOrNull() ?: continue

            if (amount < minimum || amount >= maximum) continue

            Log.d("TSPAY", "Matching amount: $amount")

            val card = findOrderCard(node) ?: continue
            val claimButton = findClaimButton(card) ?: continue

            if (clickNode(claimButton)) {
                Log.d("TSPAY", "Claim clicked for $amount")
                return true
            }
        }

        return false
    }

    private fun collectNodes(
        node: AccessibilityNodeInfo,
        result: MutableList<AccessibilityNodeInfo>
    ) {
        result.add(node)

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            collectNodes(child, result)
        }
    }

    private fun findOrderCard(
        amountNode: AccessibilityNodeInfo
    ): AccessibilityNodeInfo? {

        var current = amountNode.parent
        var levels = 0

        while (current != null && levels < 5) {
            if (findClaimButton(current) != null) {
                return current
            }
            current = current.parent
            levels++
        }

        return null
    }

    private fun findClaimButton(
        node: AccessibilityNodeInfo
    ): AccessibilityNodeInfo? {

        val text = node.text?.toString()?.trim()
        val description = node.contentDescription?.toString()?.trim()

        if (
            text.equals("Claim", ignoreCase = true) ||
            description.equals("Claim", ignoreCase = true)
        ) {
            return node
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findClaimButton(child)
            if (found != null) return found
        }

        return null
    }

    private fun clickNode(node: AccessibilityNodeInfo): Boolean {

        var current: AccessibilityNodeInfo? = node

        while (current != null) {
            if (current.isClickable) {
                return current.performAction(
                    AccessibilityNodeInfo.ACTION_CLICK
                )
            }
            current = current.parent
        }

        val rect = Rect()
        node.getBoundsInScreen(rect)

        if (rect.isEmpty) return false

        val path = Path().apply {
            moveTo(rect.exactCenterX(), rect.exactCenterY())
        }

        val gesture = GestureDescription.Builder()
            .addStroke(
                GestureDescription.StrokeDescription(path, 0, 100)
            )
            .build()

        return dispatchGesture(gesture, null, null)
    }

    private fun scrollDown(root: AccessibilityNodeInfo): Boolean {

        val scrollable = findScrollable(root)

        if (scrollable != null) {
            return scrollable.performAction(
                AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
            )
        }

        val metrics = resources.displayMetrics
        val x = metrics.widthPixels / 2f
        val startY = metrics.heightPixels * 0.78f
        val endY = metrics.heightPixels * 0.30f

        val path = Path().apply {
            moveTo(x, startY)
            lineTo(x, endY)
        }

        val gesture = GestureDescription.Builder()
            .addStroke(
                GestureDescription.StrokeDescription(path, 0, 450)
            )
            .build()

        return dispatchGesture(gesture, null, null)
    }

    private fun findScrollable(
        node: AccessibilityNodeInfo
    ): AccessibilityNodeInfo? {

        if (node.isScrollable) return node

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findScrollable(child)
            if (found != null) return found
        }

        return null
    }

    override fun onInterrupt() {
        busy = false
        Log.d("TSPAY", "Service interrupted")
    }
}
