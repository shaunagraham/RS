package com.rap.sheet.view.common

import android.app.Dialog
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.rap.sheet.R
import org.koin.android.ext.android.inject

open class BaseFragment : Fragment() {
    val sharedPreferences: SharedPreferences by inject()
    lateinit var progressDialog: Dialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showProgressDialog()
    }

    fun showProgressDialog(isCancelable: Boolean = false): Dialog {
        progressDialog = Dialog(requireActivity())
        progressDialog.setContentView(R.layout.progress_dialog_view)
        progressDialog.setCancelable(isCancelable)
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