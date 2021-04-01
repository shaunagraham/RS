package com.rap.sheet.view.common

import android.app.Dialog
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rap.sheet.R

import org.koin.android.ext.android.inject

open class BaseActivity : AppCompatActivity() {
    val sharedPreferences: SharedPreferences by inject()
    private lateinit var progressDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showProgressDialog()
    }

    fun showProgressDialog(): Dialog {
        progressDialog = Dialog(this)
        progressDialog.setContentView(R.layout.progress_dialog_view)
        progressDialog.setCancelable(false)
        progressDialog.window?.setBackgroundDrawable(ColorDrawable(0))
        return progressDialog
    }

    open fun launchProgressDialog() {
        if (::progressDialog.isInitialized && progressDialog != null && !progressDialog.isShowing)
            progressDialog.show()
    }

    /*
     * Hide progress bar when doing api call or heavy task on main thread
     */
    fun dismissProgressDialog() {
        if (::progressDialog.isInitialized && progressDialog != null && progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }


}