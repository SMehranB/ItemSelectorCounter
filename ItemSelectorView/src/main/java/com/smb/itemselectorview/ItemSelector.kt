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

class ItemSelector : View {
    constructor(context: Context): super(context) {
        initAttributes(context, null)
    }
    constructor(context: Context, attributeSet: AttributeSet?): super(context, attributeSet){
        initAttributes(context, attributeSet)
    }

    private enum class Direction {LEFT, RIGHT}

    private var animatorSet: AnimatorSet? = null
    private var clickAnimatorSet: AnimatorSet? = null
    private val clickEffectClipper = Rect()
    var animationDuration: Long = 300

    private val textPaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG)
    private val drawablePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val clickPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG or TextPaint.SUBPIXEL_TEXT_FLAG)

    private var mBackgroundColor: Int = Color.MAGENTA
    private var mShadowColor: Int = 0
    private var mShadowDy: Float = 0f
    private var mShadowDx: Float = 0f
    private var mShadowRadius: Float = 0f
    private var shadowHorizontalMargin: Float = 0f
    private var shadowVerticalMargin: Float = 0f
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

    var items: MutableList<String> = arrayListOf("Item 1", "Item 2", "Item 3")
        set(value) {
            field = value
            currentItem = value[0]
            requestLayout()
        }

    private var currentItem: String = items[0]

    private var currentItemIndex: Int = 0
    private var currentItemIndexDecoy: Int = 0

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

    private var onButtonClickListener: OnButtonClickListener? = null


    private fun initAttributes(context: Context, attributeSet: AttributeSet?) {

        val attrs = context.theme.obtainStyledAttributes(attributeSet, R.styleable.ItemSelector, 0, 0)
        attrs.apply {

            mBackgroundColor = getInteger(R.styleable.ItemSelector_is_backgroundColor, mBackgroundColor)
            mShadowColor = getInteger(R.styleable.ItemSelector_is_shadowColor, 0)
            mShadowDx = getFloat(R.styleable.ItemSelector_is_shadowDx, 0f)
            mShadowDy = getFloat(R.styleable.ItemSelector_is_shadowDy, 0f)
            mShadowRadius = getFloat(R.styleable.ItemSelector_is_shadowRadius, 0f)
            shadowHorizontalMargin = mShadowDx.plus(mShadowRadius)
            shadowVerticalMargin = mShadowDy.plus(mShadowRadius)

            verticalPadding = getDimension(R.styleable.ItemSelector_is_verticalPadding, verticalPadding)
            horizontalPadding = getDimension(R.styleable.ItemSelector_is_horizontalPadding, horizontalPadding)

            cornerRadius = getDimension(R.styleable.ItemSelector_is_cornerRadius, cornerRadius)

            drawableSize = getDimension(R.styleable.ItemSelector_is_drawableSize, drawableSize.toFloat()).toInt()
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
                currentItem = items[0]
            }

            recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        setTextParams()

        val desiredWidth = textPaint.measureText(getLongestItem()) + horizontalPadding.times(2) +
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
        setBackgroundParams()

        textClipRecF.set(buttonDimensions.width, 0f, width.minus(buttonDimensions.width).minus(shadowHorizontalMargin), height.toFloat())

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
                    buttonDimensions.width, backgroundRecF.bottom.minus(dividerMargin), drawablePaint)
            drawLine(backgroundRecF.right.minus(buttonDimensions.width), dividerMargin,
                    backgroundRecF.right.minus(buttonDimensions.width), backgroundRecF.bottom.minus(dividerMargin), drawablePaint)

            //DRAW TEXT
            save()
            clipRect(textClipRecF)
            drawText(currentItem, textX, textY, textPaint)
            restore()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if (event != null) {
            if (event.x in 0f..buttonDimensions.width) {
                //tapped left
                onButtonClickListener?.onLeftClicked(this, items[currentItemIndexDecoy], items[decAssignDecoyIndex()], currentItemIndexDecoy)
                clickAnimation(Direction.LEFT)
                slideOut(Direction.LEFT)
            }else if (event.x in width.minus(buttonDimensions.width).minus(shadowHorizontalMargin)..width.minus(shadowHorizontalMargin)) {
                //tapped right
                onButtonClickListener?.onRightClicked(this, items[currentItemIndexDecoy], items[incAssignDecoyIndex()], currentItemIndexDecoy)
                clickAnimation(Direction.RIGHT)
                slideOut(Direction.RIGHT)
            }
        }

        return super.onTouchEvent(event)
    }

    private fun slideOut(direction: Direction) {

        animatorSet?.cancel()

        val destination = if (direction == Direction.RIGHT) {
            textClipRecF.left - textPaint.measureText(currentItem).div(2)
        } else {
            textClipRecF.right.plus(textPaint.measureText(currentItem).div(2))
        }

        val slideAnimation = ValueAnimator.ofFloat(textX, destination)
        slideAnimation.addUpdateListener {
            textX = it.animatedValue as Float
            invalidate()
        }

        animatorSet = AnimatorSet()
        animatorSet?.apply {
            interpolator = AccelerateInterpolator()
            addListener(object : MyAnimatorListener {
                override fun onAnimationEnd(p0: Animator?) {
                    val textWidth = textPaint.measureText(currentItem)
                    textX = if (destination < textClipRecF.left) {
                        currentItem = items[incAssignItemIndex()]
                        width.plus(textWidth)
                    }else{
                        currentItem = items[decAssignItemIndex()]
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

    private fun slideIn() {
        val slideAnimation = ValueAnimator.ofFloat(textX, width.minus(shadowHorizontalMargin).div(2f))
        slideAnimation.addUpdateListener {
            textX = it.animatedValue as Float
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

    private fun clickAnimation(direction: Direction) {

        clickAnimatorSet?.cancel()

        if (direction == Direction.RIGHT) {
            clickEffectClipper.set(width.minus(buttonDimensions.width).minus(shadowHorizontalMargin).toInt(),
                    0, width.minus(shadowHorizontalMargin).toInt(), height)
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

    private fun decAssignItemIndex(): Int {
        return if (currentItemIndex <= 0) {
            currentItemIndex = items.lastIndex
            currentItemIndex
        }else{
            currentItemIndex = currentItemIndex.dec()
            currentItemIndex
        }
    }

    private fun decAssignDecoyIndex(): Int {
        return if (currentItemIndexDecoy <= 0) {
            currentItemIndexDecoy = items.lastIndex
            currentItemIndexDecoy
        }else{
            currentItemIndexDecoy = currentItemIndexDecoy.dec()
            currentItemIndexDecoy
        }
    }

    private fun incAssignDecoyIndex(): Int {
        return if (currentItemIndexDecoy >= items.lastIndex) {
            currentItemIndexDecoy = 0
            currentItemIndexDecoy
        }else{
            currentItemIndexDecoy = currentItemIndexDecoy.inc()
            currentItemIndexDecoy
        }
    }

    private fun incAssignItemIndex(): Int {
        return if (currentItemIndex >= items.lastIndex) {
            currentItemIndex = 0
            currentItemIndex
        }else{
            currentItemIndex = currentItemIndex.inc()
            currentItemIndex
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

    fun setTextParams(size: Int = 16, color: Int = Color.DKGRAY, style: Int = Typeface.NORMAL, @FontRes font: Int = 0) {
        textSize = dpToPixel(size)
        textColor = color
        textStyle = style
        textFont = font
        requestLayout()
    }

    fun getCurrentItem(): String {
        return currentItem
    }

    fun getCurrentItemIndex(): Int {
        return currentItemIndex
    }

    fun setOnButtonClickListener(itemSelectorButtonClickListener: OnButtonClickListener) {
        onButtonClickListener = itemSelectorButtonClickListener
    }

    private fun prepareDrawables() {
        val arrowLeft = ContextCompat.getDrawable(context, R.drawable.arrow_left_24)
        val arrowRight = ContextCompat.getDrawable(context, R.drawable.arrow_right_24)
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
    }

    private fun setBackgroundParams() {
        backgroundRecF.set(0f, 0f, width.minus(shadowHorizontalMargin), height.minus(shadowVerticalMargin))
        backgroundPaint.apply {
            color = mBackgroundColor
            if (mShadowColor != 0) {
                setShadowLayer(mShadowRadius, mShadowDx, mShadowDy, mShadowColor)
            }
        }

    }

    private fun setTextParams() {

        textPaint.apply {
            textSize = this@ItemSelector.textSize
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

    private fun getLongestItem(): String {
        var result = items[0].trim()
        for (item in items) {
            if (item.trim().length > result.length) {
                result = item
            }
        }

        return result
    }

    private fun dpToPixel(dp: Int): Float {
        return dp.times(resources.displayMetrics.density)
    }

    interface OnButtonClickListener {
        fun onLeftClicked(view: ItemSelector, oldItem: String, newItem: String, newItemIndex: Int)
        fun onRightClicked(view: ItemSelector, oldItem: String, newItem: String, newItemIndex: Int)
    }

    private inner class ButtonDimensions () {
        var rawWidth: Float = drawableSize.toFloat()
        var rawHeight: Float = drawableSize.toFloat()

        var width: Float = rawWidth
        var height: Float = rawHeight

        init {
            width = rawWidth.plus(drawableHorizontalPadding.times(2))
            height = this@ItemSelector.height.toFloat()
        }

    }
}
