package fr.neamar.kiss.ui

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView

/**
 * A custom ScrollView that intelligently decides whether to handle a scroll gesture itself
 * or to let a child view (like a scrollable widget) handle it.
 *
 * This implementation correctly checks if the touch event occurs within the bounds of a
 * child that is capable of vertical scrolling.
 */
class InterceptingScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ScrollView(context, attrs, defStyleAttr) {

    /**
     * This method is the key to the solution. It is called when a touch event is detected,
     * and it must decide if the parent (this ScrollView) should "intercept" the event
     * and start scrolling, or if it should pass the event down to the child views.
     *
     * @return `true` to intercept and scroll this view, `false` to let a child handle it.
     */
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        // Do not intercept if the touch event is within a child view that can scroll vertically.
        if (isTouchInScrollableChild(ev)) {
            // By returning false, we let the child view handle the touch event.
            return false
        }

        // Otherwise, use the default behavior of the ScrollView.
        // The default behavior will check if a scroll gesture is happening and intercept if so.
        return super.onInterceptTouchEvent(ev)
    }

    /**
     * Checks if a touch event is occurring inside a descendant view that is capable of
     * vertical scrolling.
     */
    private fun isTouchInScrollableChild(ev: MotionEvent): Boolean {
        // The raw coordinates of the touch event on the screen.
        val screenX = ev.rawX
        val screenY = ev.rawY

        // Use a temporary array to get the location of this ScrollView on the screen.
        val loc = IntArray(2)
        this.getLocationOnScreen(loc)
        val parentScreenX = loc[0]
        val parentScreenY = loc[1]

        // Calculate the touch coordinates relative to this ScrollView.
        val relativeX = screenX - parentScreenX
        val relativeY = screenY - parentScreenY

        // Find the deepest view at the touch coordinates.
        val hitView = findChildViewUnder(this, relativeX, relativeY)

        // Check if the hit view or any of its parents can scroll vertically.
        // We traverse up the hierarchy from the hit view until we reach this ScrollView.
        var view: View? = hitView
        while (view != null && view !== this) {
            // The canScrollVertically() method checks if a view can scroll up (-1) or down (1).
            // If it can do either, it's a scrollable view.
            if (view.canScrollVertically(-1) || view.canScrollVertically(1)) {
                return true
            }
            // Move up to the next parent view.
            val parent = view.parent
            view = if (parent is View) parent else null
        }

        // If no scrollable child was found under the touch point, return false.
        return false
    }

    /**
     * Recursively finds the deepest child view at a given point within a parent ViewGroup.
     */
    private fun findChildViewUnder(parent: ViewGroup, x: Float, y: Float): View {
        for (i in parent.childCount - 1 downTo 0) {
            val child = parent.getChildAt(i)
            val childBounds = Rect()
            child.getHitRect(childBounds) // Gets the view's bounds relative to its parent.

            if (childBounds.contains(x.toInt(), y.toInt())) {
                // If the child is a ViewGroup, search deeper.
                if (child is ViewGroup) {
                    // Adjust coordinates to be relative to the child's bounds for the recursive call.
                    return findChildViewUnder(child, x - child.left, y - child.top)
                } else {
                    // If it's not a ViewGroup, it's the view we hit.
                    return child
                }
            }
        }
        // If no child was hit, the parent itself is the target.
        return parent
    }
}
