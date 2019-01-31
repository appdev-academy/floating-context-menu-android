package appdev.academy.floatingcontextmenu

import android.content.Context
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.support.v4.content.ContextCompat
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewManager
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.dip
import org.jetbrains.anko.windowManager

fun View.performLongTapFeedback() = performHapticFeedback(
    HapticFeedbackConstants.LONG_PRESS,
    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING or HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
)

fun View.performHapticFeedback() = performHapticFeedback(
    HapticFeedbackConstants.VIRTUAL_KEY,
    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING or HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
)

fun Context.createShape(
    backgroundColor: Int,
    radius: Int = dip(20)
): Drawable {
    return GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = radius.toFloat()
        setColor(ContextCompat.getColor(this@createShape, backgroundColor))
    }
}

fun View.setShape(
    backgroundColor: Int,
    radius: Int = dip(20)
) {
    background = context.createShape(backgroundColor, radius)
}


private val Context.screenSizePoint: Point
    get() {
        val point = Point()
        windowManager.defaultDisplay.getSize(point)
        return point
    }
/**
 * Screen width in pixels
 */
val Context.screenWidth: Int
    get() = screenSizePoint.x

/**
 * Screen height in pixels
 */
val Context.screenHeight: Int
    get() = screenSizePoint.y

inline fun ViewManager.floatingMenu(items: List<FloatingMenu.MenuItem>, init: FloatingMenu.() -> Unit) =
    ankoView(::FloatingMenu, 0, init).apply {
        menuItems = items
    }


var View.isVisible: Boolean
    get() = visibility == View.VISIBLE
    set(value) {
        visibility = if (value) View.VISIBLE else View.INVISIBLE
    }