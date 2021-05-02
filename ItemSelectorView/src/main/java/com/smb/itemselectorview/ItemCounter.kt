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


class ItemCounter: View {

    constructor(context: Context): super(context) {
        initAttributes(context, null)
    }
    constructor(context: Context, attributeSet: AttributeSet?): super(context, attributeSet){
        initAttributes(context, attributeSet)
    }

    private enum class Operation { INC, DEC }

    private var animatorSet: AnimatorSet? = null
    private var clickAnimatorSet: AnimatorSet? = null
    private val clickEffectClipper = Rect()
    var animationDuration: Long = 300

    private val textPaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG)
    private val drawablePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val clickPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG or TextPaint.SUBPIXEL_TEXT_FLAG)

    private var mBackgroundColor: Int = Color.LTGRAY
    private val backgroundRecF = RectF()
    private var cornerRadius: Float = dpToPixel(8)

    private lateinit var buttonDimensions: ButtonDimensions
    private var arrowLeftBitmap: Bitmap? = null
    private var arrowRightBitmap: Bitmap? = null
    private var drawableLeftX: Float = 0f
    private var drawableY: Float = 0f
    private var drawableRightX: Float = 0f
    private var drawableSize: Int = dpToPixel(32).toInt()
    private var drawableHorizontalPadding: Float = 0f

    var drawableTint: Int = Color.DKGRAY
        set(value) {
            field = value
            invalidate()
        }

    private val dividerMargin: Float = dpToPixel(5)
    var dividerColor = Color.DKGRAY
        set(value) {
            field = value
            invalidate()
        }

//    var items: MutableList<Int> = arrayListOf("Item 1")
//        set(value) {
//            field = value
//            currentItem = value[0]
//            requestLayout()
//        }

    private var currentNumber: Int = 0
    private var currentItemIndex: Int = 0

    private val textClipRecF = RectF()
    private var textHeight: Float = 0f
    private var textX: Float = 0f
    private var textY: Float = 0f
    private var textSize: Float = dpToPixel(16)
    private var textColor: Int = Color.DKGRAY
    private var textStyle: Int = Typeface.NORMAL
    private var verticalPadding = dpToPixel(16)
    private var horizontalPadding = dpToPixel(16)

    @FontRes
    private var textFont: Int = 0


    private fun initAttributes(context: Context, attributeSet: AttributeSet?) {

        val attrs = context.theme.obtainStyledAttributes(attributeSet, R.styleable.ItemCounter, 0, 0)
        attrs.apply {

            mBackgroundColor = getInteger(R.styleable.ItemCounter_ic_backgroundColor, mBackgroundColor)

            verticalPadding = getDimension(R.styleable.ItemCounter_ic_verticalPadding, verticalPadding)
            horizontalPadding = getDimension(R.styleable.ItemCounter_ic_horizontalPadding, horizontalPadding)

            cornerRadius = getDimension(R.styleable.ItemCounter_ic_cornerRadius, cornerRadius)

            drawableSize = getDimension(R.styleable.ItemCounter_ic_drawableSize, drawableSize.toFloat()).toInt()
            drawableHorizontalPadding = getDimension(R.styleable.ItemCounter_ic_drawableHorizontalPadding, 0f)
            dividerColor = getInteger(R.styleable.ItemCounter_ic_dividerColor, ColorUtils.blendARGB(mBackgroundColor, Color.BLACK, 0.5f))
            drawableTint = getInteger(R.styleable.ItemCounter_ic_drawableTint, drawableTint)

            textSize = getDimension(R.styleable.ItemCounter_ic_textSize, textSize)
            textColor = getInteger(R.styleable.ItemCounter_ic_textColor, textColor)
            textStyle = getInt(R.styleable.ItemCounter_ic_textStyle, textStyle)
            textFont = getResourceId(R.styleable.ItemCounter_ic_textFont, textFont)

            animationDuration = getInt(R.styleable.ItemCounter_ic_animationDuration, animationDuration.toInt()).toLong()

            recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        setTextParams()

        val desiredWidth = textPaint.measureText(currentNumber.toString()) + horizontalPadding.times(2) +
                drawableSize.times(2) + drawableHorizontalPadding.times(4)
        val desiredHeight = (textHeight + verticalPadding.times(2)).coerceAtLeast(drawableSize.toFloat())

        val finalWidth = getFinalDimension(widthMeasureSpec, desiredWidth)
        val finalHeight = getFinalDimension(heightMeasureSpec, desiredHeight)

        setMeasuredDimension(finalWidth, finalHeight)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {

        textX = width.div(2f)
        textY = height.div(2f).plus(textHeight)

        prepareDrawables()
        setBackgroundParams()

        textClipRecF.set(buttonDimensions.width, 0f, width.minus(buttonDimensions.width), height.toFloat())

        super.onLayout(changed, left, top, right, bottom)
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.apply {
            //DRAW BACKGROUND
            drawRoundRect(backgroundRecF, cornerRadius, cornerRadius, backgroundPaint)

            save()
            clipRect(clickEffectClipper)
            drawRoundRect(backgroundRecF, cornerRadius, cornerRadius, clickPaint)
            restore()

            //DRAWING THE TWO ARROWS
            arrowLeftBitmap?.let { drawBitmap(it, drawableLeftX, drawableY, drawablePaint) }
            arrowRightBitmap?.let { drawBitmap(it, drawableRightX, drawableY, drawablePaint) }

            //DRAWING DIVIDERS
            drawLine(buttonDimensions.width, dividerMargin,
                    buttonDimensions.width, height.minus(dividerMargin), drawablePaint)
            drawLine(width.minus(buttonDimensions.width), dividerMargin,
                    width.minus(buttonDimensions.width), height.minus(dividerMargin), drawablePaint)

            //LASTLY DRAW TEXT BECAUSE IT WILL BE CLIPPED
            save()
            clipRect(textClipRecF)
            drawText(currentNumber.toString(), textX, textY, textPaint)
            restore()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if (event != null) {
            if (event.x in 0f..buttonDimensions.width) {
                //tapped to Decrement
                clickAnimation(Operation.DEC)
                if (currentNumber > 0) {
                    slideOut(Operation.DEC)
                }
            }else if (event.x in width.minus(buttonDimensions.width)..width.toFloat()) {
                //tapped to Increment
                clickAnimation(Operation.INC)
                slideOut(Operation.INC)
            }
        }

        return super.onTouchEvent(event)
    }

    private fun slideOut(operation: Operation) {

        animatorSet?.cancel()

        val destination = if (operation == Operation.INC) {
            height + textHeight
        } else {
            -textHeight
        }

        val slideAnimation = ValueAnimator.ofFloat(textY, destination)
        slideAnimation.addUpdateListener {
            textY = it.animatedValue as Float
            invalidate()
        }

        animatorSet = AnimatorSet()
        animatorSet?.apply {
            interpolator = AccelerateInterpolator()
            addListener(object : MyAnimatorListener {
                override fun onAnimationEnd(p0: Animator?) {
                    textY = if (operation == Operation.INC) {
                        currentNumber = currentNumber.inc()
                        -textHeight
                    }else{
                        currentNumber = decNumber()
                        height.plus(textHeight)
                    }
                    slideIn()
                }
            })
            duration = animationDuration
            play(slideAnimation)
            start()
        }

    }

    private fun slideIn() {
        val slideAnimation = ValueAnimator.ofFloat(textY, height.div(2f).plus(textHeight))
        slideAnimation.addUpdateListener {
            textY = it.animatedValue as Float
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

    private fun clickAnimation(operation: Operation) {

        clickAnimatorSet?.cancel()

        if (operation == Operation.INC) {
            clickEffectClipper.set(width.minus(buttonDimensions.width).toInt(), 0, width, height)
        }else{
            clickEffectClipper.set(0, 0, buttonDimensions.width.toInt(), height)
        }

        val initialColor = ColorUtils.blendARGB(mBackgroundColor, Color.WHITE, 0.7f)

        val clickAnimation = ValueAnimator.ofFloat(1f, 0f)
        clickAnimation.addUpdateListener {
            val factor = it.animatedValue as Float
            clickPaint.color = ColorUtils.blendARGB(mBackgroundColor, initialColor, factor)
            invalidate()
        }

        clickAnimatorSet = AnimatorSet()
        clickAnimatorSet!!.apply {
            duration = 300
            play(clickAnimation)
            start()
        }
    }

    private fun decNumber(): Int {
        if (currentNumber > 0) {
            return currentNumber.dec()
        }
        return 0
    }

    fun setTextPadding(verticalPaddingDp: Int = 16, horizontalPaddingDp: Int = 16) {
        verticalPadding = dpToPixel(verticalPaddingDp)
        horizontalPadding = dpToPixel(horizontalPaddingDp)
        requestLayout()
    }

    fun setDrawableParams(sizeDp: Int = 32, horizontalPaddingDp: Int = 0) {
        drawableSize = dpToPixel(sizeDp).toInt()
        drawableHorizontalPadding = dpToPixel(horizontalPaddingDp)
        requestLayout()
    }

    fun setBackgroundParams(backgroundColor: Int, radiusDp: Int = 8) {
        cornerRadius = dpToPixel(radiusDp)
        mBackgroundColor = backgroundColor
    }

    fun setTextParams(size: Int = 16, color: Int = Color.DKGRAY, style: Int = Typeface.NORMAL, @FontRes font: Int = 0) {
        textSize = dpToPixel(size)
        textColor = color
        textStyle = style
        textFont = font
        requestLayout()
    }

    fun getCurrentItem(): Int {
        return currentNumber
    }

    fun getCurrentItemIndex(): Int {
        return currentItemIndex
    }

    private fun prepareDrawables() {
        val arrowLeft = ContextCompat.getDrawable(context, R.drawable.minus_24)
        val arrowRight = ContextCompat.getDrawable(context, R.drawable.plus_24)
        arrowLeft?.let { setTint(it, drawableTint) }
        arrowRight?.let { setTint(it, drawableTint) }

        buttonDimensions = ButtonDimensions()

        arrowLeftBitmap = arrowLeft?.toBitmap(buttonDimensions.rawWidth.toInt(), buttonDimensions.rawHeight.toInt(), Bitmap.Config.ARGB_8888)
        arrowRightBitmap = arrowRight?.toBitmap(buttonDimensions.rawWidth.toInt(), buttonDimensions.rawHeight.toInt(), Bitmap.Config.ARGB_8888)

        drawablePaint.color = dividerColor
        drawablePaint.strokeWidth = dpToPixel(1)

        drawableY = height.div(2f).minus(buttonDimensions.rawHeight.div(2))

        drawableLeftX = drawableHorizontalPadding
        drawableRightX = width.minus(buttonDimensions.rawWidth).minus(drawableHorizontalPadding)
    }

    private fun setBackgroundParams() {
        backgroundRecF.set(0f, 0f, width.toFloat(), height.toFloat())
        backgroundPaint.color = mBackgroundColor
    }

    private fun setTextParams() {

        textPaint.apply {
            textSize = this@ItemCounter.textSize
            textHeight = getTextHeight()
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

//    private fun getLongestItem(): String {
//        var result = items[0].trim()
//        for (item in items) {
//            if (item.trim().length > result.length) {
//                result = item
//            }
//        }
//
//        return result
//    }

    private fun dpToPixel(dp: Int): Float {
        return dp.times(resources.displayMetrics.density)
    }

    inner class ButtonDimensions () {
        var rawWidth: Float = drawableSize.toFloat()
        var rawHeight: Float = drawableSize.toFloat()

        var width: Float = rawWidth
        var height: Float = rawHeight

        init {
            width = rawWidth.plus(drawableHorizontalPadding.times(2))
            height = this@ItemCounter.height.toFloat()
        }

    }
}