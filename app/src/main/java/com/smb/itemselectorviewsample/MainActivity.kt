package com.smb.itemselectorviewsample

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.smb.itemselectorview.ItemSelector

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val itemSelector = ItemSelector(this)
        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        itemSelector.apply {
            layoutParams = params
            items = arrayListOf("Short", "Tall", "Grande", "Venti")
            setTextParams(40, Color.CYAN, Typeface.BOLD)
            dividerColor = Color.BLACK
            setBackgroundParams(Color.BLUE, 25)
            animationDuration = 500L
            drawableTint = Color.MAGENTA
            setDrawableParams(100)
//            setTextPadding(16, 32)

        }

        val viewHolder = findViewById<ViewGroup>(R.id.viewHolder)
        viewHolder.addView(itemSelector)

    }
}