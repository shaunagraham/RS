package com.rap.sheet.utilitys

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import com.rap.sheet.R
import com.rap.sheet.extenstion.lightFont
import com.rap.sheet.extenstion.regularFont

class CustomTextViewLight : AppCompatTextView {

    constructor(context: Context) : super(context) {
        init()

    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {

        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    fun init() {
        if (!isInEditMode) {
            try {
                typeface = context.lightFont()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}