package appdev.academy.floatingcontextmenu

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.animation.Animation
import android.view.animation.OvershootInterpolator
import android.view.animation.ScaleAnimation
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import org.jetbrains.anko.*
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin


/**
 * Created by stas on 1/29/19.
 */

class FloatingMenu : FrameLayout {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    // customization
    /**
     * Menu item diameter in dp
     */
    var menuItemSize = 70

    /**
     * Menu item diameter in degrees (0..360)
     */
    var menuAngleValue = 130

    /**
     * Distance between touch position and center of menu items in dp
     */
    var menuRadius = 130

    /**
     * Expand/collapse animation duration
     */
    var animationDuration = 250L

    /**
     * Menu items scale in collapsed state.
     * 0.5f = half size
     * 1f - normal size
     * 2f - doubled size
     */
    var collapseScale = 0.3f

    /**
     * Menu items scale in expanded state.
     * 0.5f = half size
     * 1f - normal size
     * 2f - doubled size
     */
    var defaultScale = 1f


    /**
     * Menu items scale while touched.
     * 0.5f = half size
     * 1f - normal size
     * 2f - doubled size
     */
    var selectedScale = 1.13f

    /**
     * Inner padding for icon in item in dp
     */
    var innerPadding: Int = 10

    /**
     * Menu item icon color resource in default state
     */
    var defaultIconTintResource = android.R.color.black

    /**
     * Menu item icon color resource in selected state
     */
    var selectedIconTintResource = android.R.color.white


    /**
     * Menu item background color resource in default state
     */
    var defaultBackgroundTintResource: Int = android.R.color.white

    /**
     * Menu item background color resource in selected state
     */
    var selectedBackgroundTintResource: Int = android.R.color.black

    /**
     * Menu background frame color resource
     */
    var frameBgColorResource = R.color.shadow

    /**
     * Menu item title text color
     */
    var textColorResource = android.R.color.white

    /**
     * Menu items list.
     * You can assign new list.
     * Changes will be applied automatically.
     */
    var menuItems: List<MenuItem>? = null
        set(value) {
            field = value
            createMenuItemViews()
        }

    /**
     * Call this method when you change `menuItems` without reassigning.
     */
    fun notifyMenuItemsChanged() {
        createMenuItemViews()
    }

    // internal
    companion object {
        var currentlyExpandedMenu: FloatingMenu? = null
        var lastMotionEvent: MotionEvent? = null


        fun interceptTouch(ev: MotionEvent): Boolean {
            lastMotionEvent = ev
            currentlyExpandedMenu?.let {
                it.dispatchTouchEvent(ev)
                return true
            }
            return false
        }

        private const val MENU_ITEM_TAG = "menu.item"
    }

    private var aX: Int = 0
    private var aY: Int = 0

    private var selectedItemIndex = -1

    var onItemSelected: ((itemIndex: Int) -> Unit)? = null


    private var items: List<ItemComponent>? = null


    private var title: TextView

    init {
        isVisible = false
        title = textView {
            textSize = 25f
            padding = dip(25)
            textColorResource = this@FloatingMenu.textColorResource
        }
        backgroundColorResource = frameBgColorResource

    }

    private fun createMenuItemViews() {

        applyRecursively {
            if (it.tag == MENU_ITEM_TAG) {
                removeView(it)
            }
        }

        items = menuItems?.map {
            ItemComponent(it.icon).apply {
                addView(createView(AnkoContext.create(context, this@FloatingMenu)))
            }
        } ?: emptyList()
    }

    private fun checkSelectedItem(x: Int, y: Int) =
        items?.indexOfFirst { it.isSelected(x, y) }

    private val MotionEvent.xCoord: Int
        get() {
            val t = IntArray(2).apply { getLocationOnScreen(this) }
            return rawX.roundToInt() - t[0]
        }

    private val MotionEvent.yCoord: Int
        get() {
            val t = IntArray(2).apply { getLocationOnScreen(this) }
            return rawY.roundToInt() - t[1]
        }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (currentlyExpandedMenu != null) {
            if (event.action == MotionEvent.ACTION_UP) {
                isVisible = false
                if (selectedItemIndex != -1) onItemSelected?.invoke(selectedItemIndex)
                items?.forEach { it.collapse() }
                currentlyExpandedMenu = null

            }
            if (event.action == MotionEvent.ACTION_MOVE) {
                val p = checkSelectedItem(event.xCoord, event.yCoord) ?: -1
                if (p != selectedItemIndex) {
                    performHapticFeedback()

                    title.text = ""
                    items?.getOrNull(selectedItemIndex)?.setIsHighlighted(false)


                    items?.getOrNull(p)?.apply {
                        title.text = menuItems?.get(p)?.title
                        setIsHighlighted(true)
                    }
                    selectedItemIndex = p
                }

            }

            return true
        } else {
            return false
        }
    }


    @SuppressLint("RtlHardcoded")
    fun onLongTapDetected() {
        val e = lastMotionEvent ?: return
        if (e.action == MotionEvent.ACTION_UP) return
        if (items.isNullOrEmpty()) return

        currentlyExpandedMenu = this
        isVisible = true
        title.text = ""
        performLongTapFeedback()
        aX = e.xCoord
        aY = e.yCoord
        selectedItemIndex = -1
        items?.forEach {
            it.bg.layoutParams = (it.bg.layoutParams as? LayoutParams)?.apply {
                leftMargin = aX - dip(menuItemSize / 2)
                topMargin = aY - dip(menuItemSize / 2)
                gravity = Gravity.TOP or Gravity.LEFT
            }
            it.setIsHighlighted(false, false)
        }
        val leftSide = context.screenWidth / 2 > aX

        val sign = if (leftSide) 1 else -1

        val topSide = context.screenHeight / 2 > aY
        val fromDegrees = sign * if (topSide) 0 else 90
        val step = sign * (menuAngleValue) / items?.size!!


        title.layoutParams = LayoutParams(wrapContent, wrapContent).apply {
            gravity = (if (leftSide) Gravity.RIGHT else Gravity.LEFT) or
                    (if (topSide) Gravity.BOTTOM else Gravity.TOP)
        }

        items?.forEachIndexed { index, item ->
            val degree = (fromDegrees + step * index).toDouble()
            val d = Math.toRadians(degree)
            item.expand((menuRadius * sin(d)).roundToInt(), (menuRadius * cos(d)).roundToInt())
        }
    }

    inner class ItemComponent(private val image: Int) :
        AnkoComponent<FloatingMenu> {
        lateinit var bg: FrameLayout
        private lateinit var icon: ImageView

        private var translationX: Int = 0
        private var translationY: Int = 0

        override fun createView(ui: AnkoContext<FloatingMenu>) = with(ui) {
            bg = frameLayout {
                tag = MENU_ITEM_TAG
                lparams(width = dip(menuItemSize), height = dip(menuItemSize))
                padding = dip(innerPadding)
                alpha = 0f
                icon = imageView(image).lparams {
                    width = matchParent
                    height = matchParent
                }
            }
            return@with bg
        }

        fun setIsHighlighted(b: Boolean, animated: Boolean = true) {
            val from = if (b) defaultScale else selectedScale
            val to = if (!b) defaultScale else selectedScale

            bg.apply {
                val scale = ScaleAnimation(
                    from, to, from, to,
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0f
                ).apply {
                    duration = if (animated) 200L else 0
                    fillAfter = true
                }
                startAnimation(scale)
                setShape(if (b) selectedBackgroundTintResource else defaultBackgroundTintResource, dip(menuItemSize))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    elevation = if (b) dip(10).toFloat() else dip(1).toFloat()
                }
            }

            icon.setColorFilter(
                ContextCompat.getColor(context, if (b) selectedIconTintResource else defaultIconTintResource),
                PorterDuff.Mode.SRC_IN
            )
        }

        fun expand(x: Int, y: Int) {
            translationX = x
            translationY = y
            bg.animate()
                .alpha(defaultScale)
                .translationX(dip(x).toFloat())
                .translationY(dip(y).toFloat())
                .scaleY(defaultScale)
                .scaleX(defaultScale)
                .setDuration(animationDuration)
                .setInterpolator(OvershootInterpolator(2.5f))
                .start()
        }

        fun collapse() {
            bg.animate()
                .alpha(0f)
                .translationX(0f)
                .translationY(0f)
                .scaleY(collapseScale)
                .scaleX(collapseScale)
                .setDuration(animationDuration)
                .setInterpolator(FastOutSlowInInterpolator())
                .start()
        }

        fun isSelected(x: Int, y: Int): Boolean {
            val halfSize = dip(menuItemSize) / 2

            val xCenter = aX + dip(translationX)
            val xRange = (xCenter - halfSize)..(xCenter + halfSize)

            val yCenter = aY + dip(translationY)
            val yRange = (yCenter - halfSize)..(yCenter + halfSize)

            return x in xRange && y in yRange
        }

    }

    class MenuItem(
        var icon: Int,
        var title: String
    )

}