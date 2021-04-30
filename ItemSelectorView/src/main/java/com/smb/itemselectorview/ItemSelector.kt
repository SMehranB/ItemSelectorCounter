package com.smb.itemselectorview

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.annotation.FontRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
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

    private var animatorSet: AnimatorSet? = null
    var animationDuration: Long = 500

    var verticalPadding = dpToPixel(16)
    var horizontalPadding = dpToPixel(16)
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG or TextPaint.SUBPIXEL_TEXT_FLAG)

    private var mBackgroundColor: Int = Color.GRAY

    private val backgroundRecF = RectF()
    var cornerRadius: Float = dpToPixel(8)
    private val drawablePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private lateinit var buttonDimensions: ButtonDimensions
    private var arrowLeftBitmap: Bitmap? = null
    private var arrowRightBitmap: Bitmap? = null
    private var drawableLeftX: Float = 0f
    private var drawableY: Float = 0f
    private var drawableRightX: Float = 0f
    var drawableDimen: Int = dpToPixel(32).toInt()
    var drawableHorizontalPadding: Float = 0f
    var drawableTint: Int = Color.DKGRAY
    private val dividerMargin: Float = dpToPixel(5)
    var dividerColor = Color.DKGRAY

    private val textClipRecF = RectF()
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
    private var itemIndex: Int = 0

    private fun initAttributes(context: Context, attributeSet: AttributeSet?) {

        val attrs = context.theme.obtainStyledAttributes(attributeSet, R.styleable.ItemSelector, 0, 0)
        attrs.apply {

            mBackgroundColor = getInteger(R.styleable.ItemSelector_is_backgroundColor, mBackgroundColor)

            verticalPadding = getDimension(R.styleable.ItemSelector_is_verticalPadding, verticalPadding)
            horizontalPadding = getDimension(R.styleable.ItemSelector_is_horizontalPadding, horizontalPadding)

            cornerRadius = getDimension(R.styleable.ItemSelector_is_cornerRadius, cornerRadius)

            drawableDimen = getDimension(R.styleable.ItemSelector_is_buttonSize, drawableDimen.toFloat()).toInt()
            drawableHorizontalPadding = getDimension(R.styleable.ItemSelector_is_drawableHorizontalPadding, 0f)
            dividerColor = getInteger(R.styleable.ItemSelector_is_dividerColor, ColorUtils.blendARGB(mBackgroundColor, Color.BLACK, 0.5f))
            drawableTint = getInteger(R.styleable.ItemSelector_is_drawableTint, drawableTint)

            textSize = getDimension(R.styleable.ItemSelector_is_textSize, textSize)
            textColor = getInteger(R.styleable.ItemSelector_is_textColor, textColor)
            textStyle = getInt(R.styleable.ItemSelector_is_textStyle, textStyle)
            textFont = getResourceId(R.styleable.ItemSelector_is_textFont, textFont)

            animationDuration = getInt(R.styleable.ItemSelector_is_animationDuration, animationDuration.toInt()).toLong()

            val itemsArray = getResourceId(R.styleable.ItemSelector_is_items, 0)
            if (itemsArray != 0) {
                items = resources.getStringArray(itemsArray).toMutableList()
            }

            recycle()
        }

        val arrowLeft = ContextCompat.getDrawable(context, R.drawable.arrow_left_24)
        val arrowRight = ContextCompat.getDrawable(context, R.drawable.arrow_right_24)
        arrowLeft?.let { setTint(it, drawableTint) }
        arrowRight?.let { setTint(it, drawableTint) }

        arrowLeftBitmap = arrowLeft?.toBitmap(drawableDimen, drawableDimen, Bitmap.Config.ARGB_8888)
        arrowRightBitmap = arrowRight?.toBitmap(drawableDimen, drawableDimen, Bitmap.Config.ARGB_8888)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        setTextParams()

        val desiredWidth = textPaint.measureText(mText) + horizontalPadding.times(2) +
                drawableDimen.times(2) + drawableHorizontalPadding.times(4)
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

        textClipRecF.set(buttonDimensions.width, 0f, width.minus(buttonDimensions.width), height.toFloat())

        super.onLayout(changed, left, top, right, bottom)
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.apply {
            //DRAW BACKGROUND
            drawRoundRect(backgroundRecF, cornerRadius, cornerRadius, backgroundPaint)

            //DRAWING THE TWO ARROWS
            arrowLeftBitmap?.let {
                drawBitmap(it, drawableLeftX, drawableY, drawablePaint)
            }
            arrowRightBitmap?.let {
                drawBitmap(it, drawableRightX, drawableY, drawablePaint)
            }

            //DRAWING DIVIDERS
            drawLine(buttonDimensions.width, dividerMargin,
                    buttonDimensions.width, height.minus(dividerMargin), drawablePaint)
            drawLine(width.minus(buttonDimensions.width), dividerMargin,
                    width.minus(buttonDimensions.width), height.minus(dividerMargin), drawablePaint)

            //LASTLY DRAW TEXT BECAUSE IT WILL BE CLIPPED
            clipRect(textClipRecF)
            drawText(mText, textX, textY, textPaint)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if (event != null) {
            if (event.x in 0f..buttonDimensions.width) {
                //tapped left
                slideOut(textClipRecF.left - textPaint.measureText(mText).div(2))
            }else if (event.x in width.minus(buttonDimensions.width)..width.toFloat()) {
                //tapped right
                slideOut(textClipRecF.right.plus(textPaint.measureText(mText).div(2)))
            }
        }

        return super.onTouchEvent(event)
    }

    private fun slideOut(destination: Float) {

        animatorSet?.cancel()

        val slideAnimation = ValueAnimator.ofFloat(textX, destination)
        slideAnimation.addUpdateListener {
            val x = it.animatedValue as Float
            textX = x
            invalidate()
        }

        animatorSet = AnimatorSet()
        animatorSet?.apply {
            interpolator = AccelerateInterpolator()
            addListener(object : MyAnimatorListener {
                override fun onAnimationEnd(p0: Animator?) {
                    textX = if (destination < textClipRecF.left) {
                        mText = getPreviousItem()
                        val textWidth = textPaint.measureText(mText)
                        width.plus(textWidth)
                    }else{
                        mText = getNextItem()
                        val textWidth = textPaint.measureText(mText)
                        -textWidth
                    }
                    slideIn()
                }
            })
            duration = animationDuration
            play(slideAnimation)
            start()
        }

    }

    private fun getNextItem(): String {
        return if (itemIndex >= items.lastIndex) {
            itemIndex = 0
            items[itemIndex]
        }else{
            itemIndex = itemIndex.inc()
            items[itemIndex]
        }
    }

    private fun getPreviousItem(): String {
        return if (itemIndex <= 0) {
            itemIndex = items.lastIndex
            items[itemIndex]
        }else{
            itemIndex = itemIndex.dec()
            items[itemIndex]
        }
    }

    private fun slideIn() {
        val slideAnimation = ValueAnimator.ofFloat(textX, width.div(2f))
        slideAnimation.addUpdateListener {
            val x = it.animatedValue as Float
            textX = x
            invalidate()
        }

        val animatorSet = AnimatorSet()
        animatorSet.apply {
            interpolator = DecelerateInterpolator()
            duration = animationDuration
            play(slideAnimation)
            start()
        }
    }

    private fun setDrawableParams() {
        buttonDimensions = ButtonDimensions(arrowLeftBitmap!!)

        drawablePaint.color = dividerColor
        drawablePaint.strokeWidth = dpToPixel(1)

        drawableY = height.div(2f).minus(drawableDimen.div(2))

        drawableLeftX = drawableHorizontalPadding
        drawableRightX = width.minus(drawableDimen).minus(drawableHorizontalPadding)
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

    inner class ButtonDimensions (bitmap: Bitmap) {
        private val w: Float = bitmap.width.toFloat()
        private val h: Float = bitmap.height.toFloat()

        var width: Float = w
        var height: Float = h

        init {
            width = w.plus(drawableHorizontalPadding.times(2))
            height = this@ItemSelector.height.toFloat()
        }

    }
}
