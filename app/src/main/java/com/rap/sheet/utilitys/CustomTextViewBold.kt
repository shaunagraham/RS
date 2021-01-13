package com.rap.sheet.utilitys

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import com.rap.sheet.R
import com.rap.sheet.extenstion.boldFont
import com.rap.sheet.extenstion.regularFont

class CustomTextViewBold : AppCompatTextView {

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
                typeface = context.boldFont()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}