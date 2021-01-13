package com.rap.sheet.view.profile

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import com.rap.sheet.BuildConfig
import com.rap.sheet.R
import com.rap.sheet.extenstion.beGone
import com.rap.sheet.extenstion.beVisible
import com.rap.sheet.extenstion.click
import com.rap.sheet.extenstion.displayAlertDialog
import com.rap.sheet.utilitys.InternetConnection
import com.rap.sheet.view.common.BaseActivity
import com.rap.sheet.viewmodel.ProfileDetailViewModel
import kotlinx.android.synthetic.main.fragment_profile_detail.*
import kotlinx.android.synthetic.main.progress_dialog_view.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel

//import com.rap.sheet.databinding.FragmentProfileDetailBinding;

class ProfileDetailActivity : BaseActivity() {
    private var number: String? = null
    private val mViewViewModel: ProfileDetailViewModel by viewModel()
    private var name: String? = null
    private var latitude: String? = null
    private var longitude: String? = null

    //private var restInterface: RestInterface? = null
    private var isAddress: Boolean = true
    private var isImage: Boolean = true
    private var isGender: Boolean = true

//    private var imageViewAvatar: ImageView? = null
//    private var constraintLayoutProfileDetail: ConstraintLayout? = null
//    private var linearLayoutProgressBar: LinearLayout? = null
//    private var textViewAddress: TextView? = null
//    private var textViewGender: TextView? = null
//    private var textViewCity: TextView? = null
//
//    //private Toolbar toolbar;
//    private var textViewLineProvider: TextView? = null
//    private var textViewZip: TextView? = null
//    private var textViewLocation: TextView? = null
//    private var textViewCarrier: TextView? = null
//    private var textViewCarrierName: TextView? = null
//    private var textViewMMSEmail: TextView? = null
//    private var textViewUserName: TextView? = null
//    private var textViewLineName: TextView? = null
//    private var textViewCname: TextView? = null
//    private var textViewSMSEmail: TextView? = null
//    private var textViewPhoneNumber: TextView? = null
//    private var linearLayoutLineProvider: LinearLayout? = null

    //    private FragmentProfileDetailBinding binding;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_profile_detail)
        //        binding= DataBindingUtil.setContentView(this,R.layout.fragment_profile_detail);
//        restInterface = RetrofitClient.getClient(BuildConfig.EVERYONE_API).create(RestInterface::class.java)
        getIntentData()
        initView()
        setUpToolBar()
        listenToViewModel()
    }

    private fun setUpToolBar() {
//        toolbar.setTitle("");
//        setSupportActionBar(toolbar);
////        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onBackPressed();
//            }
//        });
//        binding.toolbar.imgClose.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onBackPressed();
//            }
//        });
//        binding.toolbar.tvTitle.setText(getResources().getString(R.string.profile));
    }

    private fun getIntentData() {
        intent.extras?.apply {
            val data = this.getString(ARG_PARAM1).toString().split(" ").toTypedArray()
            number = data[0]
            name = data[1]
        }
    }

    private fun initView() {
        textViewLocation.click {
            val url = "http://maps.google.com/maps?q=$latitude,$longitude"
            val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }
        mViewViewModel.getProfileDetail(number.toString(), BuildConfig.ACCOUNT_SID, BuildConfig.ACCOUNT_TOKEN)
    }

    private fun listenToViewModel() {

        mViewViewModel.profileDetailSuccessResponse.observe(this, Observer {
            setUpProfileData(it.string())
        })
        mViewViewModel.profileDetailErrorResponse.observe(this, Observer {
            this.displayAlertDialog(desc = resources.getString(R.string.something_wrong), cancelable = false, positiveText = resources.getString(android.R.string.ok), positiveClick = object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    dialog?.apply {
                        this.dismiss()
                        finish()
                    }
                }

            })
        })
        mViewViewModel.noInternetException.observe(this, Observer {
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

    @SuppressLint("SetTextI18n")
    private fun setUpProfileData(result: String) {
        try {
            val jsonObjectResult = JSONObject(result)
            if (jsonObjectResult.has("missed")) {
                val jsonArrayMissed: JSONArray = jsonObjectResult.getJSONArray("missed")
                for (i in 0 until jsonArrayMissed.length()) {
                    when {
                        jsonArrayMissed.get(i) == "address" -> {
                            isAddress = false
                        }
                        jsonArrayMissed.get(i) == "image" -> {
                            isImage = false
                        }
                        jsonArrayMissed.get(i) == "gender" -> {
                            isGender = false
                        }
                    }
                }
            }
            val jsonObjectData: JSONObject = jsonObjectResult.getJSONObject("data")
            if (jsonObjectData.has("location")) {
                val jsonObjectLocation: JSONObject = jsonObjectData.getJSONObject("location")
                val strLocation: String = "<font color='#000000'> Location: </font>" + jsonObjectLocation.getString("city") + ", " + jsonObjectLocation.getString("state") + ", " + jsonObjectLocation.getString("zip")
                textViewCity.text = Html.fromHtml(strLocation)

                if (jsonObjectLocation.has("geo")) {
                    val jsonObjectGEO: JSONObject = jsonObjectLocation.getJSONObject("geo")
                    latitude = jsonObjectGEO.getString("latitude")
                    longitude = jsonObjectGEO.getString("longitude")
                } else {
                    textViewLocation.beGone()
                }
            }
            if (jsonObjectData.has("line_provider")) {
                val jsonObjectLineProvider: JSONObject = jsonObjectData.getJSONObject("line_provider")
                linearLayoutLineProvider.beVisible()
                if (jsonObjectLineProvider.has("mms_email")) textViewMMSEmail!!.text = jsonObjectLineProvider.getString("mms_email") else textViewMMSEmail.beGone()

                if (jsonObjectLineProvider.has("sms_email")) {
                    textViewSMSEmail.text = jsonObjectLineProvider.getString("sms_email")
                } else {
                    textViewSMSEmail.beGone()
                }
            } else {
                textViewLineProvider.beGone()
                linearLayoutLineProvider.beGone()
            }
            if (jsonObjectData.has("cnam")) {
                val strCNAME: String = "<font color='#000000'> Type: </font>" + jsonObjectData.getString("cnam")
                textViewCname.text = Html.fromHtml(strCNAME)
            } else {
                textViewCname.beGone()
            }
            if (jsonObjectData.has("carrier")) {
                val jsonObjectCarrier: JSONObject = jsonObjectData.getJSONObject("carrier")
                if (jsonObjectCarrier.has("name")) {
                    val strCarrier: String = "<font color='#000000'> Carrier: </font>" + jsonObjectCarrier.getString("name")
                    textViewCarrier.text = Html.fromHtml(strCarrier)
                } else {
                    textViewCarrier.beGone()
                }
            } else {
                textViewCarrier.beGone()
                textViewCarrierName.beGone()
            }
            if (jsonObjectData.has("name")) {
                textViewUserName.text = "Name: " + jsonObjectData.getString("name")
            } else {
                textViewUserName.text = "Name: $name"
            }
            if (jsonObjectData.has("number")) {
                textViewPhoneNumber.text = "Number: " + jsonObjectData.getString("number")
            } else {
                textViewPhoneNumber.text = "Number: $number"
            }
            if (isAddress) {
                if (jsonObjectData.has("address")) {
                    textViewAddress.text = jsonObjectData.getString("address")
                    textViewAddress.visibility = View.VISIBLE
                }
            } else {
                textViewAddress.beGone()
            }
            if (isImage) {
                if (jsonObjectData.has("image")) {
                    Log.d("Hello", jsonObjectData.getString("large"))
                    imageViewAvatar.beVisible()
                }
            } else {
                imageViewAvatar.beGone()
            }
            if (isGender) {
                if (jsonObjectData.has("gender")) {
                    val strGender: String = "<font color='#000000'> Gender: </font>" + jsonObjectData.getString("gender")
                    textViewGender.text = Html.fromHtml(strGender)
                } else {
                    textViewGender.beGone()
                }
            } else {
                textViewGender.beGone()
            }
            linearLayoutProgressBar.beGone()
            constraintLayoutProfileDetail.beVisible()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    companion object {
        private val ARG_PARAM1: String = "param1"
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewViewModel.onDetach()
    }
}