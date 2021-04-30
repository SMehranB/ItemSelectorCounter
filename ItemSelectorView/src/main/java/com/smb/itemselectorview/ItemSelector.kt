package com.smb.itemselectorview

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.FontRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat.setTint
import androidx.core.graphics.drawable.toBitmap
import kotlin.math.abs

class ItemSelector : View {
    constructor(context: Context): super(context) {
        initAttributes(context, null)
    }
    constructor(context: Context, attributeSet: AttributeSet?): super(context, attributeSet){
        initAttributes(context, attributeSet)
    }

    var verticalPadding = dpToPixel(16)
    var horizontalPadding = dpToPixel(16)

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var mBackgroundColor: Int = Color.LTGRAY
    private val backgroundRecF = RectF()
    var cornerRadius: Float = dpToPixel(8)

    private val drawablePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var arrowLeftBitmap: Bitmap? = null
    private var arrowRightBitmap: Bitmap? = null
    private var drawableLeftX: Float = 0f
    private var drawableY: Float = 0f
    private var drawableRightX: Float = 0f
    var drawableDimen: Int = dpToPixel(32).toInt()
    var dividerColor = Color.GRAY

    private var textHeight: Float = 0f
    private val textPaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG)
    private var textX: Float = 0f
    private var textY: Float = 0f
    private var mText: String = "Item 1"
    private var textSize: Float = dpToPixel(16)
    var textColor: Int = Color.DKGRAY
    var textStyle: Int = Typeface.NORMAL

    @FontRes
    var textFont: Int = 0

    private var items: MutableList<String> = arrayListOf(mText)

    private fun initAttributes(context: Context, attributeSet: AttributeSet?) {

        val attrs = context.theme.obtainStyledAttributes(attributeSet, R.styleable.ItemSelector, 0, 0)
        attrs.apply {

            verticalPadding = getDimension(R.styleable.ItemSelector_is_verticalPadding, verticalPadding)
            horizontalPadding = getDimension(R.styleable.ItemSelector_is_horizontalPadding, horizontalPadding)

            cornerRadius = getDimension(R.styleable.ItemSelector_is_cornerRadius, cornerRadius)

            drawableDimen = getDimension(R.styleable.ItemSelector_is_buttonSize, drawableDimen.toFloat()).toInt()
            dividerColor = getInteger(R.styleable.ItemSelector_is_dividerColor, dividerColor)

            textSize = getDimension(R.styleable.ItemSelector_is_textSize, textSize)
            textColor = getInteger(R.styleable.ItemSelector_is_textColor, textColor)
            textStyle = getInt(R.styleable.ItemSelector_is_textStyle, textStyle)
            textFont = getResourceId(R.styleable.ItemSelector_is_textFont, 0)

            val itemsArray = getResourceId(R.styleable.ItemSelector_is_items, 0)
            if (itemsArray != 0) {
                items = resources.getStringArray(itemsArray).toMutableList()
            }

            recycle()
        }

        val arrowLeft = ContextCompat.getDrawable(context, R.drawable.arrow_left_18)
        val arrowRight = ContextCompat.getDrawable(context, R.drawable.arrow_right_24)
        arrowLeft?.let { setTint(it, Color.DKGRAY) }
        arrowRight?.let { setTint(it, Color.DKGRAY) }

        arrowLeftBitmap = arrowLeft?.toBitmap(drawableDimen, drawableDimen, Bitmap.Config.ARGB_8888)
        arrowRightBitmap = arrowRight?.toBitmap(drawableDimen, drawableDimen, Bitmap.Config.ARGB_8888)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        setTextParams()
        val desiredWidth = textPaint.measureText(mText) + horizontalPadding.times(2) + drawableDimen.times(2)
        val desiredHeight = (textHeight + verticalPadding.times(2)).coerceAtLeast(drawableDimen.toFloat())

        val finalWidth = getFinalDimension(widthMeasureSpec, desiredWidth)
        val finalHeight = getFinalDimension(heightMeasureSpec, desiredHeight)

        setMeasuredDimension(finalWidth, finalHeight)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        textX = width.div(2f)
        textY = height.div(2f).plus(textHeight)

        setDrawableParams()
        setBackgroundParams()

        super.onLayout(changed, left, top, right, bottom)
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.apply {
            drawRoundRect(backgroundRecF, cornerRadius, cornerRadius, backgroundPaint)

            drawText(mText, textX, textY, textPaint)

            //DRAWING THE TWO ARROWS
            arrowLeftBitmap?.let {
                drawBitmap(it, drawableLeftX, drawableY, drawablePaint)
            }
            arrowRightBitmap?.let {
                drawBitmap(it, drawableRightX, drawableY, drawablePaint)
            }

            //DRAWING DIVIDERS
            drawLine(drawableLeftX.plus(drawableDimen), dpToPixel(3), drawableLeftX.plus(drawableDimen), height.minus(dpToPixel(3)), drawablePaint)
            drawLine(drawableRightX, dpToPixel(3), drawableRightX, height.minus(dpToPixel(3)), drawablePaint)
        }
    }

    private fun setDrawableParams() {

        drawablePaint.color = dividerColor

        drawableY = height.div(2f).minus(drawableDimen.div(2))

        drawableLeftX = 0f
        drawableRightX = width.minus(drawableDimen).toFloat()
    }

    private fun setBackgroundParams() {

        backgroundRecF.set(0f, 0f, width.toFloat(), height.toFloat())

        backgroundPaint.apply {
            color = mBackgroundColor
        }
    }

    private fun setTextParams() {
        textHeight = getTextHeight()

        textPaint.apply {
            textSize = this@ItemSelector.textSize
            color = textColor
            textAlign = Paint.Align.CENTER

            typeface = Typeface.create(Typeface.DEFAULT, textStyle)
            if (textFont != 0) {
                val tf = ResourcesCompat.getFont(context, textFont)
                typeface = Typeface.create(tf, textStyle)
            }
        }
    }

    private fun getTextHeight(): Float {
        return abs(textPaint.ascent().plus(textPaint.descent()).div(2))
    }

    private fun getFinalDimension(measureSpec: Int, desiredSize: Float): Int {

        val size = MeasureSpec.getSize(measureSpec)

        return when (MeasureSpec.getMode(measureSpec)) {
            MeasureSpec.EXACTLY -> {
                size
            }

            MeasureSpec.AT_MOST -> {
                size.coerceAtMost(desiredSize.toInt())
            }

            else -> {
                desiredSize.toInt()
            }
        }
    }

    private fun dpToPixel(dp: Int): Float {
        return dp.times(resources.displayMetrics.density)
    }
}
