package com.rap.sheet.utilitys

import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.TypefaceSpan

/**
 * Created by mvayak on 24-03-2018.
 */
class CustomTypefaceSpan constructor(family: String?, private val newType: Typeface?) : TypefaceSpan(family) {
    override fun updateDrawState(ds: TextPaint) {
        applyCustomTypeFace(ds, newType)
    }

    override fun updateMeasureState(paint: TextPaint) {
        applyCustomTypeFace(paint, newType)
    }

    companion object {
        private fun applyCustomTypeFace(paint: Paint, tf: Typeface?) {
            val oldStyle: Int
            val old: Typeface? = paint.typeface
            if (old == null) {
                oldStyle = 0
            } else {
                oldStyle = old.style
            }
            val fake: Int = oldStyle and tf!!.style.inv()
            if ((fake and Typeface.BOLD) != 0) {
                paint.isFakeBoldText = true
            }
            if ((fake and Typeface.ITALIC) != 0) {
                paint.textSkewX = -0.25f
            }
            paint.typeface = tf
        }
    }

}