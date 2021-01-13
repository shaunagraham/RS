package com.rap.sheet.application

import android.app.Application
import android.content.Context
import android.os.Build
import android.preference.PreferenceManager
import com.rap.sheet.db.AdsRemovePref
import com.rap.sheet.di.mainAppModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level


class BaseApplication : Application() {

    val premiumTag = "premiumTag"
    private var isPremium = false

    companion object {

        var adsRemovePref: AdsRemovePref? = null

        lateinit var baseAppClass: BaseApplication
        // var userPref: UserPref? = null
    }

//    var baseAppClass: BaseApplication? = null
//    private lateinit var baseAppClass: BaseApplication

    override fun onCreate() {
        super.onCreate()
        // Fabric.with(this, Crashlytics())
        //  userPref = UserPref(this)
        adsRemovePref = AdsRemovePref(this)
        baseAppClass = this
        //        MobileAds.initialize(this, "ca-app-pub-7827617590051418~1041478222");
        //AppSharedPreference.init(this)
        startKoin {
            androidLogger(Level.DEBUG)
            // Android context
            androidContext(this@BaseApplication)
            // modules
            modules(mainAppModules)

        }

    }


    fun getInstance(): BaseApplication {
        return baseAppClass
    }


}