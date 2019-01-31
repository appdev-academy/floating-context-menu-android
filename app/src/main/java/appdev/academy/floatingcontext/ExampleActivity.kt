package appdev.academy.floatingcontext

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.widget.ArrayAdapter
import appdev.academy.floatingcontextmenu.FloatingMenu
import appdev.academy.floatingcontextmenu.floatingMenu
import org.jetbrains.anko.*

/**
 * Created by stas on 1/29/19.
 */

class ExampleActivity : AppCompatActivity() {

    private lateinit var menu: FloatingMenu

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        UI(true) {
            relativeLayout {
                val list = listView {
                    setOnItemLongClickListener { _, _, position, _ ->
                        menu.apply {
                            onItemSelected = { toast("Item $position menu option $it") }
                            onLongTapDetected()
                        }
                        true
                    }

                    setOnItemClickListener { _, _, position, _ ->
                        toast("item $position selected")
                    }
                }

                menu = floatingMenu(ArrayList<FloatingMenu.MenuItem>().apply {
                    (0..2).forEach { add(FloatingMenu.MenuItem(R.drawable.search_icon, "Option$it")) }
                }) {}.lparams {
                    width = matchParent
                    height = matchParent
                }

                list.adapter = ArrayAdapter<String>(
                    context,
                    android.R.layout.simple_list_item_1,
                    ArrayList<String>().apply { (0 until 20).forEach { add("Element#$it") } }
                )

            }
        }

    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        // Let Floating menu handle event if it can
        return if (FloatingMenu.interceptTouch(ev))
            true
        else
            super.dispatchTouchEvent(ev)
    }
}

