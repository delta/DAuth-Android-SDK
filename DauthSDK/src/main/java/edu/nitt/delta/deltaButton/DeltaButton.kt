package edu.nitt.delta.deltaButton

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import androidx.core.content.ContextCompat
import edu.nitt.delta.R
import edu.nitt.delta.helpers.ViewUtils.dpToPixel
import edu.nitt.delta.helpers.ViewUtils.drawableToBitmap

/**
 * An open [DeltaButton] class to integrate with the client UI
 */
open class DeltaButton : androidx.appcompat.widget.AppCompatButton {

    /**
     * Default customisable properties of the button
     * Users can override these default properties in XML while using this button class
     */
    private var mIcon: Bitmap? = null
    private var textColors: Int = Color.WHITE
    private var mIconPadding = 0
    private var mIconSize = 0
    private var mRoundedCornerRadius = 0
    private var mIconCenterAligned = false
    private var mRoundedCorner = false
    private var mTransparentBackground = false
    private var mBackgroundColor: Int = Color.BLACK


    /**
     * Canvas utils
     */
    private var mPaint: Paint? = null
    private var mSrcRect: Rect? = null
    private lateinit var drawable: GradientDrawable

    /**
     * Constructor
     *
     * @param context Context
     */
    constructor(context: Context) : super(context)

    /**
     * Constructor
     *
     * @param context Context
     * @param attrs   AttributeSet
     */
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs, R.drawable.delta_logo)
        setStyle(R.color.default_background_color, context)
    }

    /**
     * Constructor
     *
     * @param context      Context
     * @param attrs        AttributeSet
     * @param defStyleAttr Int
     */
    constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int,
    ) :
            super(context, attrs, defStyleAttr) {
        init(context, attrs, R.drawable.delta_logo)
        setStyle(R.color.default_background_color, context)
    }

    /**
     * Sets the style to the button
     *
     * @param color   Int
     * @param context Context
     */
    private fun setStyle(color: Int, context: Context) {
        setTextColor(textColors)
        setText(R.string.login_button_text)
        drawable = GradientDrawable()
        drawable.shape = GradientDrawable.RECTANGLE
        drawable.setColor(mBackgroundColor)
        drawable.cornerRadius = 0f
        this.isClickable = true
        if (mRoundedCorner) {
            drawable.cornerRadius = mRoundedCornerRadius.toFloat()
        }
        if (mTransparentBackground) {
            drawable.setColor(Color.TRANSPARENT)
            drawable.setStroke(4, color)
        }
        drawable.invalidateSelf()
        background = drawable
        setPadding(
            dpToPixel(30f, context).toInt(), 0,
            dpToPixel(30f, context).toInt(), 0
        )
    }

    /**
     * Overrides the [onDraw] method of AppCompatButton class
     *
     * @param canvas Canvas
     */
    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        // Recalculate width and amount to shift by, taking into account icon size
        val shift = (mIconSize + mIconPadding) / 2
        canvas.save()
        canvas.translate(shift.toFloat(), 0f)
        super.onDraw(canvas)
        val textWidth = paint.measureText(text.toString())
        var left = (width / 2f - textWidth / 2f - mIconSize - mIconPadding).toInt()
        val top = height / 2 - mIconSize / 2
        if (!mIconCenterAligned) left = 0
        val destRect = Rect(left, top, left + mIconSize, top + mIconSize)
        mIcon?.let { canvas.drawBitmap(it, mSrcRect, destRect, mPaint) }
        canvas.restore()
    }

    /**
     * Initialize the process to get custom attributes from xml and set button params.
     *
     * @param context      Context
     * @param attrs AttributeSet
     * @param logo         Int
     */
    private fun init(context: Context, attrs: AttributeSet, logo: Int) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.DeltaButton)

        // Initialize variables to default values
        setDefaultValues(context, logo)

        // Don't add padding when text isn't present
        if (attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "text") != null) {
            mIconPadding = dpToPixel(20f, context).toInt()
        }

        textColors = array.getColor(R.styleable.DeltaButton_android_textColor, Color.WHITE)

        // Load the custom properties and assign values
        for (i in 0 until array.indexCount) {
            val attr = array.getIndex(i)
            if (attr == R.styleable.DeltaButton_iconPadding) {
                mIconPadding = array.getDimensionPixelSize(
                    attr,
                    dpToPixel(20f, context).toInt()
                )
            }
            if (attr == R.styleable.DeltaButton_iconCenterAligned) {
                mIconCenterAligned = array.getBoolean(attr, true)
            }
            if (attr == R.styleable.DeltaButton_iconSize) {
                mIconSize = array.getDimensionPixelSize(
                    attr,
                    dpToPixel(20f, context).toInt()
                )
            }
            if (attr == R.styleable.DeltaButton_roundedCorner) {
                mRoundedCorner = array.getBoolean(attr, false)
            }
            if (attr == R.styleable.DeltaButton_roundedCornerRadius) {
                mRoundedCornerRadius = array.getDimensionPixelSize(
                    attr,
                    dpToPixel(40f, context).toInt()
                )
            }
            if (attr == R.styleable.DeltaButton_transparentBackground) {
                mTransparentBackground = array.getBoolean(attr, false)
            }
            if (attr == R.styleable.DeltaButton_button_background_color) {
                mBackgroundColor = array.getColor(attr, Color.BLACK)
            }

        }
        array.recycle()
        if (mIcon != null) {
            mPaint = Paint()
            mSrcRect = Rect(0, 0, mIcon!!.width, mIcon!!.height)
        }
    }

    /**
     * Set the default values to button
     *
     * @param context Context
     * @param logo    Int
     */
    private fun setDefaultValues(context: Context, logo: Int) {
        mIcon = drawableToBitmap(ContextCompat.getDrawable(context, logo)!!)
        mIconSize = dpToPixel(20f, context).toInt()
        mIconCenterAligned = false
        mRoundedCorner = false
        mTransparentBackground = false
        mRoundedCornerRadius = dpToPixel(8f, context).toInt()
    }
}
