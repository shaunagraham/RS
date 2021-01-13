package com.rap.sheet.view.settings

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.databinding.DataBindingUtil
import com.android.billingclient.api.*
import com.rap.sheet.R
import com.rap.sheet.application.BaseApplication

import com.rap.sheet.databinding.FragmentSettingsBinding
import com.rap.sheet.extenstion.beGone
import com.rap.sheet.extenstion.click
import com.rap.sheet.extenstion.startActivityFromFragment
import com.rap.sheet.view.common.BaseFragment
import com.rap.sheet.view.feedback.FeedbackActivity
import com.rap.sheet.view.profile.ProfileActivity

/**
 * Created by mvayak on 02-08-2018.
 */

class SettingsFragment : BaseFragment(){
    private var billingClient: BillingClient?=null
    private lateinit var binding: FragmentSettingsBinding
    private val TAG = SettingsFragment::class.java.simpleName

    private var isSubscribe = false
//    val skuList: MutableList<String> = ArrayList()
    private var skuDetail:SkuDetails?=null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false)
        setView()
        return binding.root
    }

    private fun setView() {
//        skuList.add(BuildConfig.PRODUCT_ID)
  //      Log.i(TAG, "setView: " + skuList.size)
        setUpBillingClient()

        binding.lvProfile.click {
            startActivityFromFragment<ProfileActivity>()
        }
        binding.lvRateApp.click {
            try {
                val rateIntent = rateIntentForUrl("market://details")
                startActivity(rateIntent)
            } catch (e: ActivityNotFoundException) {
                val rateIntent = rateIntentForUrl("https://play.google.com/store/apps/details")
                startActivity(rateIntent)
            }
        }
        binding.lvShareApp.click {
            shareApp()
        }
        binding.lvFeedback.click {
            startActivityFromFragment<FeedbackActivity>()
        }
        binding.lvHelp.click {
            redirectToTerms(resources.getString(R.string.drawer_help), "http://www.rapsheetapp.com/faq?device=mobile")
        }
        binding.lvRemoveResult.click {
            startActivityFromFragment<RemoveResultActivity>()
        }
        binding.lvPrivacyPolicy.click {
            redirectToTerms(resources.getString(R.string.drawer_privacy), "http://www.rapsheetapp.com/privacy-policy?device=mobile")
        }
        binding.lvTerms.click {
            redirectToTerms(resources.getString(R.string.drawer_terms), "http://www.rapsheetapp.com/terms-of-use?device=mobile")
        }
        binding.lvRemoveAds.click {
            isSubscribe = true

            skuDetail?.let {
                val billingFlowParams = BillingFlowParams.newBuilder()
                        .setSkuDetails(it)
                        .build()
                billingClient?.launchBillingFlow(requireActivity(), billingFlowParams)?.responseCode
            }
        }

        if(  BaseApplication.adsRemovePref?.isAdsRemove!!){
            binding.lvRemoveAds.beGone()
        }


    }

    private fun redirectToTerms(title: String, url: String) {
        val bundle = Bundle()
        bundle.putString("title", title)
        bundle.putString("url", url)
        startActivityFromFragment<HelpPrivacyTermsActivity>(bundle)
    }

    private fun rateIntentForUrl(url: String): Intent {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(String.format("%s?id=%s", url, requireActivity().packageName)))
        val flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        intent.addFlags(flags)
        return intent
    }

    private fun shareApp() {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Check out the RapSheet App at: https://play.google.com/store/apps/details?id=" + requireActivity().packageName)
        sendIntent.type = "text/plain"
        startActivity(Intent.createChooser(sendIntent, resources.getText(R.string.drawer_share_app)))
    }

//    private fun setupBillingClient() {
//        billingClient = context?.let {
//            BillingClient.newBuilder(it)
//                    .enablePendingPurchases()
//                    .setListener(this)
//                    .build()
//        }!!
//        billingClient.startConnection(object : BillingClientStateListener {
//            override fun onBillingSetupFinished(billingResult: BillingResult) {
//                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
//                    // The BillingClient is setup successfully
//                    Log.i(TAG, "onBillingSetupFinished: " + "the BillingClient is setup successfully")
//                    loadAllSKUs()
//                }
//            }
//
//            override fun onBillingServiceDisconnected() {
//                // Try to restart the connection on the next request to
//                // Google Play by calling the startConnection() method.
//
//            }
//        })
//    }

//    private fun loadAllSKUs() = if (billingClient.isReady) {
//        val params = SkuDetailsParams
//                .newBuilder()
//                .setSkusList(skuList)
//                .setType(BillingClient.SkuType.INAPP)
//                .build()
//        billingClient.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
//            // Process the result.
//            Log.i(TAG, "loadAllSKUs: " + skuList!!.size)
//            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
//                Log.i(TAG, "loadAllSKUs:11 " + billingResult.responseCode)
//                if (skuDetailsList != null) {
//                    for (skuDetails in skuDetailsList) {
//                        if (skuDetails.sku == "test_product_one")
//                        binding.lvDisclaimer.click {
//                            val billingFlowParams = BillingFlowParams
//                                    .newBuilder()
//                                    .setSkuDetails(skuDetails)
//                                    .build()
//                            billingClient.launchBillingFlow(context as Activity, billingFlowParams)
//                        }
//
//                        //this will return both the SKUs from Google Play Console
//                    }
//                }
//            }
//        }
//    } else {
//        println("Billing Client not ready")
//    }

//    private fun acknowledgePurchase(purchaseToken: String) {
//        val params = AcknowledgePurchaseParams.newBuilder()
//                .setPurchaseToken(purchaseToken)
//                .build()
//        billingClient.acknowledgePurchase(params) { billingResult ->
//            val responseCode = billingResult.responseCode
//            val debugMessage = billingResult.debugMessage
//            Log.i(TAG, "acknowledgePurchase: " + responseCode)
//            Log.i(TAG, "acknowledgePurchase: " + BaseApplication.adsRemovePref?.isAdsRemove!!)
//            if (!BaseApplication.adsRemovePref?.isAdsRemove!!) {
//                BaseApplication.adsRemovePref?.setRemoveAdsData(true);
//            }
//        }
//    }
//
//    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
//        if (billingResult?.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
//            for (purchase in purchases) {
//                acknowledgePurchase(purchase.purchaseToken)
//            }
//        } else if (billingResult?.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
//            // Handle an error caused by a user cancelling the purchase flow.
//        } else {
//            Log.i(TAG, "onPurchasesUpdated: " + purchases)
//            // Handle any other error codes.
//        }
//    }


//    private fun initBillingManager() {
//        mBillingManager = BillingUtil(context as Activity?, object : BillingUpdatesListener {
//            override fun onBillingClientSetupFinished() {
////                binding.buttonContinue.setEnabled(true)
//
//                if (mBillingManager != null
//                        && mBillingManager!!.billingClientResponseCode
//                        > BILLING_MANAGER_NOT_INITIALIZED) {
//                    onManagerReady(this@SettingsFragment)
//                    Log.i(TAG, "onBillingClientSetupFinished: ")
//                }
//            }
//
//            override fun onConsumeFinished(token: String, result: Int) {
//                if (result == BillingClient.BillingResponseCode.OK) {
//                    run {
//                        Log.i(TAG, "onConsumeFinished: ")
//                    }
//                }
//            }
//
//            override fun onPurchasesUpdated(purchases: List<Purchase>, responseCode: Int) {
//                if (purchases != null) {
//                    if (purchases.size > 0) {
//                        Log.i(TAG, "onPurchasesUpdated: ")
////                        CommonMethod.setFirebaseEvent("InAppSubscribe onPurchasesUpdated")
//                        BaseApplication.baseAppClass.setPremium(false)
//                        for (purchase in purchases) {
//                            val consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken)
//                            if (mBillingManager != null) {
//                                mBillingManager!!.consumeAsync(consumeParams.build())
//                            }
//                            if (purchase.sku == resources.getString(R.string.product_id_key)) {
//                                BaseApplication.baseAppClass.setPremium(true)
//                            }
//                        }
//                        Log.d(TAG, "onPurchasesUpdated: $responseCode")
//                    } else {
//                        BaseApplication.baseAppClass.setPremium(false)
//                    }
//                } else {
//                    BaseApplication.baseAppClass.setPremium(false)
//                }
//                if (isSubscribe) {
//                    if (responseCode == BillingClient.BillingResponseCode.OK) {
//                        showCustomDialog(true)
//                    } else {
//                        showCustomDialog(false)
//                    }
//                }
//            }
//        })
//    }

    fun showCustomDialog(isSucess: Boolean) {
        isSubscribe = false
//        val deleteDialogView: View = LayoutInflater.from(this).inflate(R.layout.custom_dialog_layout, null)
//        val deleteDialog: AlertDialog = Builder(this, R.style.CustomDialog).create()
//        deleteDialog.setView(deleteDialogView)
//        val textViewTitte = deleteDialogView.findViewById<TextView>(R.id.tvTitle)
//        val textViewMessage = deleteDialogView.findViewById<TextView>(R.id.tvMessage)
        if (isSucess) {

            Log.i(TAG, "showCustomDialog: " + "success")
//            textViewTitte.text = resources.getString(R.string.success)
//            textViewMessage.text = resources.getString(R.string.premium_success)
        } else {

            Log.i(TAG, "showCustomDialog: " + "something want to wrong")

//            textViewTitte.text = resources.getString(R.string.oops)
//            textViewMessage.text = resources.getString(R.string.something_went_wrong)
        }
//        val buttonOk: MaterialButton = deleteDialogView.findViewById(R.id.buttonYes)
//        buttonOk.text = "Ok"
//        buttonOk.setOnClickListener { v: View? ->
//            deleteDialog.dismiss()
//            onBackPressed()
//        }
//        deleteDialogView.findViewById<View>(R.id.buttonNo).visibility = View.GONE
//        deleteDialog.show()
    }



    private fun setUpBillingClient() {
        billingClient = BillingClient.newBuilder(requireContext())
                .setListener(purchaseUpdateListener)
                .enablePendingPurchases()
                .build()
        startConnection()
    }

    private val purchaseUpdateListener =  PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handleNonConcumablePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d("TAG_INAPP", "Canceled")
            BaseApplication.adsRemovePref?.setRemoveAdsData(false)
            // Handle an error caused by a user cancelling the purchase flow.
        } else {
            BaseApplication.adsRemovePref?.setRemoveAdsData(false)
            Log.d("TAG_INAPP", "Other Error "+billingResult.responseCode+" / "+billingResult.debugMessage)
            // Handle any other error codes.
        }
    }

    private fun handleNonConcumablePurchase(purchase: Purchase) {
        Log.v("TAG_INAPP","handlePurchase : $purchase")
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken).build()
                billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    val billingResponseCode = billingResult.responseCode
                    val billingDebugMessage = billingResult.debugMessage
                    BaseApplication.adsRemovePref?.setRemoveAdsData(true)
                    binding.lvRemoveAds.beGone()
                    Log.v("TAG_INAPP","response code: $billingResponseCode")
                    Log.v("TAG_INAPP","debugMessage : $billingDebugMessage")

                }
            }else{
                BaseApplication.adsRemovePref?.setRemoveAdsData(false)
                Log.v("TAG_INAPP","Error Avi")
            }
        }
    }

    private fun startConnection() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode ==  BillingClient.BillingResponseCode.OK) {
                    Log.v("TAG_INAPP","Setup Billing Done")
                    // The BillingClient is ready. You can query purchases here.
                    val pr = billingClient!!.queryPurchases(BillingClient.SkuType.INAPP)
                    val pList = pr.purchasesList
                    if(!pList.isNullOrEmpty()) {
                        for (iitem in pList) {
                            val consumeParams = ConsumeParams.newBuilder()
                                    .setPurchaseToken(iitem.purchaseToken)
                                    .build()
                            billingClient!!.consumeAsync(consumeParams) { _: BillingResult, s: String ->
                                BaseApplication.adsRemovePref?.setRemoveAdsData(true)
                            }
                        }
                    }else{
                        BaseApplication.adsRemovePref?.setRemoveAdsData(false)
                    }
                    queryAvaliableProducts()
                }
            }
            override fun onBillingServiceDisconnected() {

                Log.v("TAG_INAPP","Billing client Disconnected")
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })
    }

    private fun queryAvaliableProducts() {
        val skuList = ArrayList<String>()
        skuList.add(resources.getString(R.string.product_id_key))
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)

        billingClient?.querySkuDetailsAsync(params.build()) { billingResult, skuDetailsList ->
            // Process the result.
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && !skuDetailsList.isNullOrEmpty()) {
                for (skuDetails in skuDetailsList) {
                    skuDetail=skuDetails
                }
            }
        }
    }
}