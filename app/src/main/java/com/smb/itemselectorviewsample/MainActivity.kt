package com.smb.itemselectorviewsample

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.smb.itemselectorview.ItemSelector

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val iSelector = findViewById<ItemSelector>(R.id.pizzaSizes)
        iSelector.items = arrayListOf("Small", "Medium", "Large")

        val itemSelector = ItemSelector(this)
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.setMargins(16, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics).toInt(), 16 ,0)

        itemSelector.apply {
            layoutParams = params
            setTextParams(24, Color.YELLOW, Typeface.BOLD)
            setBackgroundParams(Color.RED, 15)
            setDrawableParams(50, 32)
            setTextPadding(32, 32)
            items = arrayListOf("Short", "Tall", "Grande", "Venti")
            dividerColor = Color.GRAY
            animationDuration = 300L
            drawableTint = Color.YELLOW
        }

        val viewHolder = findViewById<ViewGroup>(R.id.viewHolder)
        viewHolder.addView(itemSelector)
    }
}