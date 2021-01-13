package com.rap.sheet.view.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.android.billingclient.api.*
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.rap.sheet.BuildConfig
import com.rap.sheet.R
import com.rap.sheet.application.BaseApplication
import com.rap.sheet.databinding.ActivityMainBinding
import com.rap.sheet.extenstion.beGone
import com.rap.sheet.extenstion.beVisible
import com.rap.sheet.extenstion.click
import com.rap.sheet.extenstion.startActivityFromActivityWithBundleCode
import com.rap.sheet.utilitys.Constant
import com.rap.sheet.view.black_list.BlackListContactFragment
import com.rap.sheet.view.search_number.SearchNumberActivity
import com.rap.sheet.view.settings.SettingsFragment


class MainActivity : AppCompatActivity(){

    var position: Int = 0
    var doubleBackToExitPressedOnce: Boolean = false
    private lateinit var binding: ActivityMainBinding
    private var billingClient: BillingClient?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        initView()
        setUpBillingClient()
    }


    private fun setUpBillingClient() {
        billingClient = BillingClient.newBuilder(this)
                .setListener(purchaseUpdateListener)
                .enablePendingPurchases()
                .build()
        startConnection()
    }

    private val purchaseUpdateListener =  PurchasesUpdatedListener { billingResult, purchases ->

    }


    private fun initView() {

        binding.editTextSearch.click {
            val bundle = Bundle()
            bundle.putString("search", binding.editTextSearch.text.toString())
            startActivityFromActivityWithBundleCode<SearchNumberActivity>(bundle, resultCode = Constant.SEARCH_RESULT)
        }

        binding.tvTitle.beGone()
        binding.linearLayoutSearch.beVisible()
        goToFragment(HomeFragment.newInstance(binding.editTextSearch.text.toString().trim()))
        binding.bottomNavigation.menu.getItem(0).setIcon(R.drawable.ic_home_selected)

        binding.bottomNavigation.setOnNavigationItemSelectedListener(object : BottomNavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                binding.bottomNavigation.menu.getItem(0).setIcon(R.drawable.ic_home_default)
                binding.bottomNavigation.menu.getItem(1).setIcon(R.drawable.ic_search_default)
                binding.bottomNavigation.menu.getItem(2).setIcon(R.drawable.ic_block_default)
                binding.bottomNavigation.menu.getItem(3).setIcon(R.drawable.ic_settings_default)
                when (item.itemId) {
                    R.id.bottomNavigationHome -> {
                        binding.tvTitle.beGone()
                        //    item.setIcon(R.drawable.ic_home_selected)
                        binding.linearLayoutSearch.beVisible()
                        binding.editTextSearch.beVisible()
                        binding.bottomNavigation.menu.getItem(0).setIcon(R.drawable.ic_home_selected)
                        goToFragment(HomeFragment.newInstance(binding.editTextSearch.text.toString().trim()))
                    }
                    R.id.bottomNavigationMyList -> {
//                        binding.tvTitle.beGone()
//                        binding.linearLayoutSearch.beVisible()
//                        binding.editTextSearch.beVisible()
//                        binding.tvTitle.text = resources.getString(R.string.my_list)
//                       // item.setIcon(R.drawable.ic_list_selected)
//                        goToFragment(MyContactFragment.newInstance())
                        val bundle = Bundle()
                        bundle.putString("search", "")
                        startActivityFromActivityWithBundleCode<SearchNumberActivity>(bundle, resultCode = Constant.SEARCH_RESULT)
//                        startActivity(Intent(this@MainActivity, SearchNumberActivity::class.java))
                    }
                    R.id.bottomNavigationBlackList -> {
                        binding.tvTitle.beGone()
                        binding.linearLayoutSearch.beVisible()
                        binding.editTextSearch.beGone()
//                        binding.tvTitle.text = resources.getString(R.string.blackList)
                        //  item.setIcon(R.drawable.ic_block_slected)
                        goToFragment(BlackListContactFragment())
                    }
                    R.id.bottomNavigationSettings -> {
                        binding.tvTitle.beVisible()
                        binding.linearLayoutSearch.beGone()
                        binding.tvTitle.text = resources.getString(R.string.setting)
                        binding.bottomNavigation.menu.getItem(3).setIcon(R.drawable.ic_setting_selected)
                        //    item.setIcon(R.drawable.ic_settings_selected)
                        goToFragment(SettingsFragment())
                    }
                    else -> {
                        return false
                    }
                }
                return true
            }
        })
    }

    fun goToFragment(fragment: Fragment) {
        binding.editTextSearch.setText("")
        val backStateName: String = fragment.javaClass.name
        val fragmentManager: FragmentManager = supportFragmentManager
        val ft: FragmentTransaction = fragmentManager.beginTransaction()
        ft.replace(R.id.mainContain, (fragment), backStateName)
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        ft.commit()
    }

    private fun rateIntentForUrl(url: String): Intent {
        val intent: Intent = Intent(Intent.ACTION_VIEW, Uri.parse(String.format("%s?id=%s", url, packageName)))
        val flags: Int = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        intent.addFlags(flags)
        return intent
    }

    fun shareApp() {
        val sendIntent: Intent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Check out the RapSheet App at: https://play.google.com/store/apps/details?id=$packageName")
        sendIntent.type = "text/plain"
        startActivity(Intent.createChooser(sendIntent, resources.getText(R.string.drawer_share_app)))
    }

    public override fun onResume() {
//        KeyboardHide keyboardHide = new KeyboardHide(this);
//        keyboardHide.setupUI(findViewById(R.id.drawer_layout));
        super.onResume()
    }

    override fun onBackPressed() {
//        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frameContainer);
//        if (fragment != null && fragment.getClass() != null) {
//            if (fragment.getClass().getName().equals(SearchFragment.class.getName())) {
        if (doubleBackToExitPressedOnce) {
            finish()
        }
        doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Touch again to exit", Toast.LENGTH_SHORT).show()
        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)

    }

//        @Override
//        public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {
//            if (!BaseApplication.adsRemovePref.isAdsRemove()) {
//                BaseApplication.adsRemovePref.setRemoveAdsData(true);
//            }
////            hideItem();
//        }
//
//        @Override
//        public void onPurchaseHistoryRestored() {
//
//        }
//
//        @Override
//        public void onBillingError(int errorCode, @Nullable Throwable error) {
//
//        }
//
//        @Override
//        public void onBillingInitialized() {
//            if (bp.isPurchased(BuildConfig.PRODUCT_ID)) {
//                hideItem();
//            }
//        }

//
//        @Override
//        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//            if (!bp.handleActivityResult(requestCode, resultCode, data)) {
//                super.onActivityResult(requestCode, resultCode, data);
//            }
//        }

//    public override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.menu_main, menu)
//        return super.onCreateOptionsMenu(menu)
//    }
//
//    public override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if (item.itemId == R.id.action_logo) {
//            if (position != R.id.nav_item_search) {
//                //callSearchFragment();
//            }
//        }
//        return super.onOptionsItemSelected(item)
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constant.SEARCH_RESULT) {
            data?.apply {
                val search = this.getStringExtra("search")
                Log.i("TAG", "onActivityResult: " + search)
                binding.bottomNavigation.menu.getItem(0).setIcon(R.drawable.ic_home_selected)
//                initView()
                binding.tvTitle.beGone()
                goToFragment(HomeFragment.newInstance(search.toString()))
                binding.linearLayoutSearch.beVisible()
                binding.bottomNavigation.menu.getItem(0).isChecked = true
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        billingClient?.apply {
            this.endConnection()
        }
    }


    private fun startConnection() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {

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
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.v("TAG_INAPP", "Billing client Disconnected")
            }
        })
    }

}