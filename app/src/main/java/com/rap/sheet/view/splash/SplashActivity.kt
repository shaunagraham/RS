package com.rap.sheet.view.splash

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import com.rap.sheet.R
import com.rap.sheet.application.BaseApplication
import com.rap.sheet.extenstion.*
import com.rap.sheet.utilitys.InternetConnection
import com.rap.sheet.view.common.BaseActivity
import com.rap.sheet.view.home.MainActivity
import com.rap.sheet.viewmodel.SplashViewModel
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashActivity : BaseActivity() {
    private val mViewModel: SplashViewModel by viewModel()
    private lateinit var handler: Handler
    private var runnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        if (!isTaskRoot) {
            finish()
            return
        }
        Looper.getMainLooper()?.let {
            handler = Handler(it)
        }

        BaseApplication.adsRemovePref?.apply {
            this.clearUserDataPref()
        }

        val uid: String = getDeviceID()

        checkUserRegisterOrNot(uid)
        listenToViewModel()

    }

    private fun listenToViewModel() {
        mViewModel.makeJsonForCreateUser.observe(this, {
            mViewModel.createUser(it)
        })

        mViewModel.createUserSuccessResponse.observe(this, {
            val result: String = it.string()
            val jsonObjectData = JSONObject(result)
            val jsonObjectInfo = JSONObject(jsonObjectData.getString("info"))
            sharedPreferences.userID = jsonObjectInfo.getString("id")
            sharedPreferences.email = jsonObjectInfo.getString("email")
            sharedPreferences.uID = jsonObjectInfo.getString("uuid")
            initialize(false)
        })
        mViewModel.createUserErrorResponse.observe(this, {
            initialize(false)
        })
        mViewModel.unAuthorizationException.observe(this, {
            initialize(false)
        })

        mViewModel.noInternetException.observe(this, {
            if (InternetConnection.checkConnection(this)) {
                this.displayAlertDialog(
                        desc = resources.getString(R.string.something_wrong),
                        cancelable = false,
                        positiveText = resources.getString(android.R.string.ok),
                        positiveClick = { dialog, _ ->
                            dialog?.apply {
                                this.dismiss()
                                finish()
                            }
                        }
                )
            } else {
                this.displayAlertDialog(
                        desc = resources.getString(R.string.no_internet_msg),
                        cancelable = false,
                        positiveText = resources.getString(android.R.string.ok),
                        positiveClick = { dialog, _ ->
                            dialog?.apply {
                                this.dismiss()
                                finish()
                            }
                        }
                )
            }
        })
    }

    private fun checkUserRegisterOrNot(uid: String) {
        if (sharedPreferences.uID == "") {
            mViewModel.makeJsonForCreateUser(uid)
        } else {
            initialize(true)
        }
    }

    @SuppressLint("HardwareIds")
    private fun getDeviceID(): String {
        return Settings.Secure.getString(applicationContext.contentResolver,
                Settings.Secure.ANDROID_ID)
    }

    private fun initialize(isExists: Boolean) {
        runnable = Runnable { startActivityInlineWithFinishAll<MainActivity>() }
        handler.postDelayed(runnable!!, (if ((isExists)) 1000 else 1000).toLong())
    }

    override fun onBackPressed() {
        runnable?.apply {
            handler.removeCallbacks(this)
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()

        mViewModel.onDetach()
    }

}