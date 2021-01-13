package com.rap.sheet.extenstion

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.rap.sheet.R

fun View.beGone() {
    if (this.visibility != View.GONE) {
        this.visibility = View.GONE
    }
}

fun View.beVisible() {
    if (this.visibility != View.VISIBLE) {
        this.visibility = View.VISIBLE
    }
}

fun View.beInVisible() {
    if (this.visibility != View.INVISIBLE) {
        this.visibility = View.INVISIBLE
    }
}

fun String.isEmailValid(): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.isFieldEmpty(): Boolean {
    return TextUtils.isEmpty(this)
}

fun String.showToast(context: Context) {
    Toast.makeText(context, this, Toast.LENGTH_SHORT).show()
}

fun EditText.showSoftKeyboard(context: Context) {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    imm!!.showSoftInput(this, SHOW_IMPLICIT)
}

fun Context.displayAlertDialog(
        title: String = "",
        desc: String = "",
        cancelable: Boolean = true,
        positiveText: String = "",
        positiveClick: DialogInterface.OnClickListener? = null,
        negativeText: String? = null,
        negativeClick: DialogInterface.OnClickListener? = null
) {
    val alertDialogBuilder = AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(desc)
            .setCancelable(cancelable)
            .setPositiveButton(positiveText, positiveClick)
            .setNegativeButton(negativeText, negativeClick)

    val alertDialog = alertDialogBuilder.create()

    alertDialog.setOnShowListener {
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent))
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent))
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(
                        ContextCompat.getColor(
                                this,
                                R.color.colorAccent
                        )
                )
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(
                        ContextCompat.getColor(
                                this,
                                R.color.colorAccent
                        )
                )
    }
    alertDialog.show()
}

fun Context.makeSnackBar(
        coordinatorLayout: View,
        message: String,
        actionText: String = "",
        onClickListener: View.OnClickListener? = null
) {
    var snackbar: Snackbar? = null
    if (actionText == "") {
        snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT)
    } else {
        if (onClickListener != null) {
            snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_INDEFINITE)
                    .setAction(actionText, onClickListener)
            snackbar.setActionTextColor(Color.RED)
        }
    }

    snackbar?.let {
        val textViewSnackBar = it.view.findViewById<TextView>(R.id.snackbar_text)
        textViewSnackBar.typeface = this.regularFont()
        it.show()
    }

}


// Font
fun Context.regularFont(): Typeface {
    return ResourcesCompat.getFont(this, R.font.avenir_regular)!!
}

fun Context.mediumFont(): Typeface {
    return ResourcesCompat.getFont(this, R.font.avenir_medium)!!
}

fun Context.lightFont(): Typeface {
    return ResourcesCompat.getFont(this, R.font.avenir_light)!!
}

fun Context.boldFont(): Typeface {
    return ResourcesCompat.getFont(this, R.font.avenir_bold)!!
}

fun Context.boldGothamFont(): Typeface {
    return ResourcesCompat.getFont(this, R.font.gotham_bold)!!
}

fun Context.bookGothamFont(): Typeface {
    return ResourcesCompat.getFont(this, R.font.gotham_book)!!
}

fun <T : View> T.click(block: (T) -> Unit) = setOnClickListener { block(it as T) }

inline fun <reified T : Activity> Activity.startActivityInline(
        bundle: Bundle? = null,
        finish: Boolean = false) {
    startActivity(bundle?.let {
        Intent(this, T::class.java).putExtras(it)
    } ?: Intent(this, T::class.java))

    if (finish) {
        finish()
    }
}

inline fun <reified T : Activity> Activity.startActivityInlineWithFinishAll(bundle: Bundle? = null) {
    if (bundle != null) {
        startActivity(Intent(this.applicationContext, T::class.java)
                .putExtras(bundle).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    } else {
        startActivity(Intent(this.applicationContext, T::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    }
    this.finishAffinity()
}

inline fun <reified T : Activity> Fragment.startActivityFinishAllFromFragment(bundle: Bundle? = null) {
    if (bundle != null) {
        startActivity(
                Intent(this.requireContext(), T::class.java)
                        .putExtras(bundle).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    } else {
        startActivity(
                Intent(this.requireContext(), T::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    }
    requireActivity().finishAffinity()
}

inline fun <reified T : Activity> Fragment.startActivityFromFragment(
        bundle: Bundle? = null
) {
    if (bundle != null) {
        startActivity(Intent(this.context, T::class.java).putExtras(bundle))
    } else {
        startActivity(Intent(this.context, T::class.java))
    }
}

inline fun <reified T : Activity> Fragment.startActivityFromFragmentWithBundleCode(
        bundle: Bundle? = null,
        resultCode: Int
) {
    bundle?.apply {
        startActivityForResult(Intent(this@startActivityFromFragmentWithBundleCode.context, T::class.java).putExtras(this), resultCode)
    } ?: startActivityForResult(Intent(this.context, T::class.java), resultCode)
}

inline fun <reified T : Activity> Activity.startActivityFromActivityWithBundleCode(
        bundle: Bundle? = null,
        resultCode: Int
) {
    bundle?.apply {
        startActivityForResult(Intent(this@startActivityFromActivityWithBundleCode, T::class.java).putExtras(this), resultCode)
    } ?: startActivityForResult(Intent(this, T::class.java), resultCode)
//    startActivityForResult(Intent(this, T::class.java).putExtras(bundle), resultCode)
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}
