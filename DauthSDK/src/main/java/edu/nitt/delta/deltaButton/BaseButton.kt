package edu.nitt.delta.deltaButton


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
import edu.nitt.delta.deltaButton.Utils.convertDpToPixel
import edu.nitt.delta.deltaButton.Utils.drawableToBitmap
import edu.nitt.delta.helpers.isDarkThemeOn

open class BaseButton : androidx.appcompat.widget.AppCompatButton {
    private var mIcon: Bitmap? = null
    private var mPaint: Paint? = null
    private var mSrcRect: Rect? = null
    private var textColors: Int = Color.WHITE
    private var mIconPadding = 0
    private var mIconSize = 0
    private var mRoundedCornerRadius = 0
    private var mIconCenterAligned = false
    private var mRoundedCorner = false
    private var mTransparentBackground = false
    private var mBackgroundColor:Int = Color.BLACK

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int,
        color: Int,
        logo: Int
    ) :
            super(context, attrs, defStyleAttr) {
        init(context, attrs, logo)
        setStyle(color, context)
    }


    constructor(context: Context, attrs: AttributeSet, color: Int, logo: Int) :
            super(context, attrs) {
        init(context, attrs, logo)
        setStyle(color, context)
    }

    private fun setStyle(color: Int, context: Context) {
        setTextColor(textColors)
        setBackgroundResource(R.drawable.round_corner)
        val drawable = background.mutate() as GradientDrawable
        drawable.setColor(resources.getColor(color))
        drawable.cornerRadius = 0f
        if (mRoundedCorner) drawable.cornerRadius = mRoundedCornerRadius.toFloat()
        if (mTransparentBackground) {
            drawable.setColor(Color.TRANSPARENT)
            drawable.setStroke(4, resources.getColor(color))
        }
        drawable.invalidateSelf()
        setPadding(
            convertDpToPixel(30f, context).toInt(), 0,
            convertDpToPixel(30f, context).toInt(), 0
        )
    }

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

    private fun init(context: Context, attrs: AttributeSet, logo: Int) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.BaseButton)

        // Initialize variables to default values
        setDefaultValues(context, logo)

        // Don't add padding when text isn't present
        if (attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "text") != null) {
            mIconPadding = convertDpToPixel(20f, context).toInt()
        }

        textColors = array.getColor(R.styleable.BaseButton_android_textColor, Color.WHITE)



        // Load the custom properties and assign values
        for (i in 0 until array.indexCount) {
            val attr = array.getIndex(i)
            if (attr == R.styleable.BaseButton_iconPadding) {
                mIconPadding = array.getDimensionPixelSize(
                    attr,
                    convertDpToPixel(20f, context).toInt()
                )
            }
            if (attr == R.styleable.BaseButton_iconCenterAligned) {
                mIconCenterAligned = array.getBoolean(attr, true)
            }
            if (attr == R.styleable.BaseButton_iconSize) {
                mIconSize = array.getDimensionPixelSize(
                    attr,
                    convertDpToPixel(20f, context).toInt()
                )
            }
            if (attr == R.styleable.BaseButton_roundedCorner) {
                mRoundedCorner = array.getBoolean(attr, false)
            }
            if (attr == R.styleable.BaseButton_roundedCornerRadius) {
                mRoundedCornerRadius = array.getDimensionPixelSize(
                    attr,
                    convertDpToPixel(40f, context).toInt()
                )
            }
            if (attr == R.styleable.BaseButton_transparentBackground) {
                mTransparentBackground = array.getBoolean(attr, false)
            }
            if (attr == R.styleable.BaseButton_backgroundColor){
                mBackgroundColor = array.getColor(attr, Color.BLACK)
            }
        }
        array.recycle()
        if (mIcon != null) {
            mPaint = Paint()
            mSrcRect = Rect(0, 0, mIcon!!.width, mIcon!!.height)
        }
    }

    private fun setDefaultValues(context: Context, logo: Int) {
        mIcon = drawableToBitmap(ContextCompat.getDrawable(context, logo)!!)
        mIconSize = convertDpToPixel(20f, context).toInt()
        mIconCenterAligned = false
        mRoundedCorner = false
        mTransparentBackground = false
        mRoundedCornerRadius = convertDpToPixel(8f, context).toInt()

    }
}
