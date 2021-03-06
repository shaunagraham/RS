package com.rap.sheet.utilitys

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.res.ResourcesCompat
import com.rap.sheet.R
import com.rap.sheet.extenstion.regularFont

class CustomEditText : AppCompatEditText {


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
                typeface = context.regularFont()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


}