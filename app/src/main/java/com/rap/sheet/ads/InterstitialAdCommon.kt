package com.rap.sheet.ads

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.rap.sheet.BuildConfig
import com.rap.sheet.application.BaseApplication
import com.rap.sheet.view.splash.SplashActivity

object InterstitialAdCommon {

    private var interstitialAd: InterstitialAd? = null
    private var nextIntent: Intent? = null
    var interfaceForAd: InterfaceForClick? = null

    fun initInterstitial(context: Activity) {
        if (interstitialAd == null) {
            interstitialAd = InterstitialAd(context)
            interstitialAd!!.adUnitId = BuildConfig.INTERSTITIAL_ID
        }
        interstitialAd!!.adListener = object : AdListener() {
            override fun onAdLoaded() {}
            override fun onAdFailedToLoad(errorCode: Int) {}
            override fun onAdOpened() {}
            override fun onAdLeftApplication() {
                interstitialAd = null
            }

            override fun onAdClosed() {
                interfaceForAd?.onClick(0)
                if (nextIntent != null) {
                    Log.d("TTT", "onAdClosed: $context")
                    context.startActivity(nextIntent)
                    (context as? SplashActivity)?.finish()
                }
                loadInterstitial()
            }
        }
    }

    fun loadInterstitial() {
        if (!BaseApplication.adsRemovePref?.isAdsRemove!!) {
            if (interstitialAd != null && !interstitialAd!!.isLoaded) {
                val extras = Bundle()
                extras.putString("max_ad_content_rating", "G")
                val adRequest: AdRequest.Builder = AdRequest.Builder()
                        .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
                        .tagForChildDirectedTreatment(true)
                interstitialAd!!.loadAd(adRequest.build())
            }
        }
    }

    fun showInterstitial(intent: Intent?, context: Activity, interfaceForAdClick: InterfaceForClick?) {
        try {
            interfaceForAd = interfaceForAdClick
            nextIntent = intent
            if (interstitialAd != null && interstitialAd!!.isLoaded && !BaseApplication.adsRemovePref?.isAdsRemove!!) {
                interstitialAd!!.show()
            } else {
                interfaceForAd?.onClick(0)
                if (nextIntent != null) {
                    context.startActivity(nextIntent)
                    (context as? SplashActivity)?.finish()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}