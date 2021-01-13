package com.rap.sheet.view.splash

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.Observer
import com.android.billingclient.api.*
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
    //        implements BillingProcessor.IBillingHandler {
    private val mViewModel: SplashViewModel by viewModel()
    private var handler: Handler = Handler()
    private var runnable: Runnable? = null
    val TAG = SplashActivity::class.java.simpleName

    //    val skuList: MutableList<String> = ArrayList()


    //    private BillingProcessor bp;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        if (!isTaskRoot) {
            finish()
            return
        }

        // bp = new BillingProcessor(this, BuildConfig.LICENSE_KEY, this);
//        bp.initialize();

        BaseApplication.adsRemovePref?.apply {
            this.clearUserDataPref()
        }

        val uid: String = getDeviceID()

        checkUserRegisterOrNot(uid)
        listenToViewModel()

//        skuList.add(BuildConfig.PRODUCT_ID)
//        Log.i(TAG, "setView: " + skuList.size)

    }

    private fun listenToViewModel() {
        mViewModel.makeJsonForCreateUser.observe(this, Observer {
            mViewModel.createUser(it)
        })

        mViewModel.createUserSuccessResponse.observe(this, Observer {
            val result: String = it.string()
            val jsonObjectData = JSONObject(result)
            val jsonObjectInfo = JSONObject(jsonObjectData.getString("info"))
            sharedPreferences.userID = jsonObjectInfo.getString("id")
            sharedPreferences.email = jsonObjectInfo.getString("email")
            sharedPreferences.uID = jsonObjectInfo.getString("uuid")
            Log.i(TAG, "listenToViewModel: $jsonObjectInfo")
            initialize(false)
        })
        mViewModel.createUserErrorResponse.observe(this, Observer {
            Log.d("Hello", it.string())
            initialize(false)
        })
        mViewModel.unAuthorizationException.observe(this, Observer {
            Log.d("Hello", it.toString())
            initialize(false)
        })

        mViewModel.noInternetException.observe(this, Observer {
            if (InternetConnection.checkConnection(this)) {
                this.displayAlertDialog(desc = resources.getString(R.string.something_wrong), cancelable = false, positiveText = resources.getString(android.R.string.ok), positiveClick = object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        dialog?.apply {
                            this.dismiss()
                            finish()
                        }
                    }

                })
            } else {
                this.displayAlertDialog(desc = resources.getString(R.string.no_internet_msg), cancelable = false, positiveText = resources.getString(android.R.string.ok), positiveClick = object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        dialog?.apply {
                            this.dismiss()
                            finish()
                        }
                    }
                })
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