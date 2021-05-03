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
import androidx.annotation.ColorInt
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

    //ANIMATION PROPERTIES
    private var slideAnimatorSet: AnimatorSet? = null
    private var clickAnimatorSet: AnimatorSet? = null
    var animationDuration: Long = 200

    //PAINT OBJECTS
    private val textPaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG)
    private val drawablePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val clickPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val buttonsPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG or TextPaint.SUBPIXEL_TEXT_FLAG)

    //BUTTONS' PROPERTIES
    private lateinit var buttonDimensions: ButtonDimensions
    private val decButtonClipper = Rect()
    private val incButtonClipper = Rect()
    private val clickEffectClipper = Rect()
    private val decButtonRecF = RectF()
    private val incButtonRecF = RectF()
    private var incButtonColor: Int = Color.GREEN
    private var decButtonColor: Int = Color.RED

    //BACKGROUND PROPERTIES
    private var mBackgroundColor: Int = Color.LTGRAY
    private var mShadowColor: Int = 0
    private var mShadowDy: Float = 0f
    private var mShadowDx: Float = 0f
    private var mShadowRadius: Float = 0f
    private var shadowHorizontalMargin: Float = 0f
    private var shadowVerticalMargin: Float = 0f
    private val backgroundRecF = RectF()
    private var cornerRadius: Float = dpToPixel(8)

    //DRAWABLE PROPERTIES
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

    //TEXT PROPERTIES
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
    private var currentNumber: Int = 0


    private fun initAttributes(context: Context, attributeSet: AttributeSet?) {

        val attrs = context.theme.obtainStyledAttributes(attributeSet, R.styleable.ItemCounter, 0, 0)
        attrs.apply {

            mBackgroundColor = getInteger(R.styleable.ItemCounter_ic_backgroundColor, mBackgroundColor)
            mShadowColor = getInteger(R.styleable.ItemCounter_ic_shadowColor, 0)
            mShadowDx = getFloat(R.styleable.ItemCounter_ic_shadowDx, 0f)
            mShadowDy = getFloat(R.styleable.ItemCounter_ic_shadowDy, 0f)
            mShadowRadius = getFloat(R.styleable.ItemCounter_ic_shadowRadius, 0f)
            shadowHorizontalMargin = mShadowDx.plus(mShadowRadius)
            shadowVerticalMargin = mShadowDy.plus(mShadowRadius)

            verticalPadding = getDimension(R.styleable.ItemCounter_ic_verticalPadding, verticalPadding)
            horizontalPadding = getDimension(R.styleable.ItemCounter_ic_horizontalPadding, horizontalPadding)

            cornerRadius = getDimension(R.styleable.ItemCounter_ic_cornerRadius, cornerRadius)

            drawableSize = getDimension(R.styleable.ItemCounter_ic_drawableSize, drawableSize.toFloat()).toInt()
            drawableHorizontalPadding = getDimension(R.styleable.ItemCounter_ic_drawableHorizontalPadding, 0f)
            incButtonColor = getInteger(R.styleable.ItemCounter_ic_IncButtonColor, incButtonColor)
            decButtonColor = getInteger(R.styleable.ItemCounter_ic_DecButtonColor, decButtonColor)
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

        prepareText()

        val desiredWidth = textPaint.measureText(currentNumber.toString()) + horizontalPadding.times(2) +
                drawableSize.times(2) + drawableHorizontalPadding.times(4) + shadowHorizontalMargin
        val desiredHeight = (textHeight + verticalPadding.times(2)).coerceAtLeast(drawableSize.toFloat()) + shadowVerticalMargin

        val finalWidth = getFinalDimension(widthMeasureSpec, desiredWidth)
        val finalHeight = getFinalDimension(heightMeasureSpec, desiredHeight)

        setMeasuredDimension(finalWidth, finalHeight)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {

        textX = width.minus(shadowHorizontalMargin).div(2f)
        textY = height.minus(shadowVerticalMargin).div(2f).plus(textHeight)

        prepareDrawables()
        prepareBackground()

        textClipRecF.set(buttonDimensions.width, 0f, width.minus(buttonDimensions.width), height.minus(shadowVerticalMargin).toFloat())

        super.onLayout(changed, left, top, right, bottom)
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.apply {
            //DRAWING BACKGROUND
            drawRoundRect(backgroundRecF, cornerRadius, cornerRadius, backgroundPaint)

            //DRAWING BUTTON COLORS
            if (decButtonColor != mBackgroundColor) {
                save()
                clipRect(decButtonClipper)
                buttonsPaint.color = decButtonColor
                drawRoundRect(decButtonRecF, cornerRadius, cornerRadius, buttonsPaint)
                restore()
            }
            if (incButtonColor != mBackgroundColor) {
                save()
                clipRect(incButtonClipper)
                buttonsPaint.color = incButtonColor
                drawRoundRect(incButtonRecF, cornerRadius, cornerRadius, buttonsPaint)
                restore()
            }

            //DRAWING THE CLICK EFFECT
            save()
            clipRect(clickEffectClipper)
            drawRoundRect(backgroundRecF, cornerRadius, cornerRadius, clickPaint)
            restore()

            //DRAWING THE TWO ARROWS
            arrowLeftBitmap?.let { drawBitmap(it, drawableLeftX, drawableY, drawablePaint) }
            arrowRightBitmap?.let { drawBitmap(it, drawableRightX, drawableY, drawablePaint) }

            //DRAWING DIVIDERS
            drawLine(buttonDimensions.width, dividerMargin,
                    buttonDimensions.width, height.minus(dividerMargin).minus(shadowHorizontalMargin), drawablePaint)
            drawLine(width.minus(buttonDimensions.width).minus(shadowHorizontalMargin), dividerMargin,
                    width.minus(buttonDimensions.width).minus(shadowHorizontalMargin), height.minus(dividerMargin).minus(shadowVerticalMargin), drawablePaint)

            //DRAWING TEXT
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
            }else if (event.x in width.minus(buttonDimensions.width).minus(shadowHorizontalMargin)..width.minus(shadowHorizontalMargin)) {
                //tapped to Increment
                clickAnimation(Operation.INC)
                slideOut(Operation.INC)
            }
        }

        return super.onTouchEvent(event)
    }

    private fun slideOut(operation: Operation) {

        slideAnimatorSet?.cancel()

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

        slideAnimatorSet = AnimatorSet()
        slideAnimatorSet?.apply {
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
        val slideAnimation = ValueAnimator.ofFloat(textY, height.minus(shadowVerticalMargin).div(2f).plus(textHeight))
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

        val targetColor: Int
        val originalColor = if (operation == Operation.INC) {
            clickEffectClipper.set(width.minus(buttonDimensions.width).minus(shadowHorizontalMargin).toInt(), 0, width.minus(shadowHorizontalMargin).toInt(), height)
            targetColor = incButtonColor
            ColorUtils.blendARGB(incButtonColor, Color.WHITE, 0.7f)
        }else{
            clickEffectClipper.set(0, 0, buttonDimensions.width.toInt(), height)
            targetColor = decButtonColor
            ColorUtils.blendARGB(decButtonColor, Color.WHITE, 0.7f)
        }

        val clickAnimation = ValueAnimator.ofFloat(0f, 1f)
        clickAnimation.addUpdateListener {
            val factor = it.animatedValue as Float
            clickPaint.color = ColorUtils.blendARGB(originalColor, targetColor, factor)
            invalidate()
        }

        clickAnimatorSet = AnimatorSet()
        clickAnimatorSet!!.apply {
            duration = 300
            play(clickAnimation)
            start()
        }
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

    fun setButtonsColor(@ColorInt incButtonColor: Int, @ColorInt decButtonColor: Int) {
        this.incButtonColor = incButtonColor
        this.decButtonColor = decButtonColor
        invalidate()
    }

    fun setBackgroundParams(backgroundColor: Int, radiusDp: Int = 8) {
        cornerRadius = dpToPixel(radiusDp)
        mBackgroundColor = backgroundColor
        invalidate()
    }

    fun setShadowParams(@ColorInt color: Int, dx: Float, dy: Float, radius: Float) {
        mShadowRadius = radius
        mShadowDx = dx
        mShadowDy = dy
        mShadowColor = color
        shadowHorizontalMargin = dx.plus(radius)
        shadowVerticalMargin = dy.plus(radius)
        backgroundPaint.setShadowLayer(mShadowRadius, mShadowDx, mShadowDy, mShadowColor)
        requestLayout()
    }

    fun setTextParams(sizeDp: Int = 16, @ColorInt color: Int = Color.DKGRAY, style: Int = Typeface.NORMAL, @FontRes font: Int = 0) {
        textSize = dpToPixel(sizeDp)
        textColor = color
        textStyle = style
        textFont = font
        requestLayout()
    }

    fun getCurrentNumber(): Int {
        return currentNumber
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

        drawableY = height.minus(shadowVerticalMargin).div(2f).minus(buttonDimensions.rawHeight.div(2))

        drawableLeftX = drawableHorizontalPadding
        drawableRightX = width.minus(shadowHorizontalMargin).minus(buttonDimensions.rawWidth).minus(drawableHorizontalPadding)

        val incButtonClipperLeft = width.minus(buttonDimensions.width).minus(shadowHorizontalMargin).toInt()
        val incButtonClipperRight = width.minus(shadowHorizontalMargin)
        val buttonsBottom = height.minus(shadowVerticalMargin)
        incButtonRecF.set(incButtonClipperLeft.minus(cornerRadius), 0f, incButtonClipperRight, buttonsBottom)
        incButtonClipper.set(incButtonClipperLeft, 0, incButtonClipperRight.toInt(), height)

        decButtonRecF.set(0f, 0f, buttonDimensions.width.plus(cornerRadius), buttonsBottom)
        decButtonClipper.set(0, 0, buttonDimensions.width.toInt(), height)
    }

    private fun prepareBackground() {
        backgroundRecF.set(0f, 0f, width.minus(shadowHorizontalMargin), height.minus(shadowVerticalMargin))
        backgroundPaint.apply {
            color = mBackgroundColor
            if (mShadowColor != 0) {
                setShadowLayer(mShadowRadius, mShadowDx, mShadowDy, mShadowColor)
            }
        }
    }

    private fun prepareText() {

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

    private fun decNumber(): Int {
        if (currentNumber > 0) {
            return currentNumber.dec()
        }
        return 0
    }

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
