package edu.nitt.delta.deltaButton

import android.content.Context
import android.util.AttributeSet
import edu.nitt.delta.R


class DeltaButton : BaseButton {
    constructor(context: Context, attrs: AttributeSet) : super(
        context,
        attrs,
        R.color.default_background_color,
        R.drawable.deltalogo
    )

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle,
        R.color.default_background_color,
        R.drawable.deltalogo
    )

    constructor(context: Context) : super(context)
}
