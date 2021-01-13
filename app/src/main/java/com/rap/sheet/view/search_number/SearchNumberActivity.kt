package com.rap.sheet.view.search_number

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.View.OnTouchListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.rap.sheet.R
import com.rap.sheet.ads.InterfaceForClick
import com.rap.sheet.ads.InterstitialAdCommon
import com.rap.sheet.ads.InterstitialAdCommon.showInterstitial
import com.rap.sheet.databinding.ActivitySearchNumberBinding
import com.rap.sheet.db.SearchViewModel
import com.rap.sheet.extenstion.beGone
import com.rap.sheet.extenstion.beVisible
import com.rap.sheet.extenstion.click
import com.rap.sheet.extenstion.showToast
import com.rap.sheet.model.SearchConatct.SearchContactDataModel
import com.rap.sheet.model.SearchConatct.SearchContactRootModel
import com.rap.sheet.utilitys.Constant
import com.rap.sheet.utilitys.InternetConnection
import com.rap.sheet.utilitys.Utility
import com.rap.sheet.view.common.BaseActivity
import com.rap.sheet.view.contact_detail.ContactDetailActivity
import com.rap.sheet.viewmodel.HomeViewModel
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.fragment_search.buttonAddContact
import kotlinx.android.synthetic.main.no_message_view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.IOException

class SearchNumberActivity : BaseActivity() {
    lateinit var binding: ActivitySearchNumberBinding
    var isAddNew = false
    var isAPICall = false
    var isDetailAPICall = false
    var isSearch = false
    private var searchContactDataModelList: MutableList<SearchContactDataModel> = mutableListOf()
    private var pos = -1
    private val mViewModel: HomeViewModel by viewModel()
    private lateinit var searchViewModel: SearchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //        setContentView(R.layout.activity_search_number);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search_number)
        searchViewModel = ViewModelProvider(this).get(SearchViewModel::class.java)
        InterstitialAdCommon.initInterstitial(this)
        InterstitialAdCommon.loadInterstitial()
        getIntentData()
        setView()
    }

    private fun getIntentData() {
        intent.extras?.apply {
            binding.editTextNumber.setText(this.getString("search"))
            binding.editTextNumber.setSelection(binding.editTextNumber.text.toString().length)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setView() {
        //make translucent statusBar on kitkat devices
        if (Build.VERSION.SDK_INT in 19..20) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true)
            }
        }
        if (Build.VERSION.SDK_INT >= 19) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        //make fully Android Transparent Status bar
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
            window.statusBarColor = Color.TRANSPARENT
        }

        binding.buttonSearch.click {
            launchProgressDialog()
            isAddNew = false
            isDetailAPICall = true
            listenToViewModel()
            mViewModel.searchUser(binding.editTextNumber.text.toString())
        }

        //edit text
        binding.editTextNumber.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
//                binding.buttonSearch.text = resources.getString(R.string.search_no)
//                binding.editTextNumber.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_close, 0)

                if (binding.editTextNumber.text.toString().trim().isNotEmpty()) {
                    // Add drawable Right
                    binding.buttonSearch.text = resources.getString(R.string.search_no)
                    binding.editTextNumber.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_close, 0)
                } else {
                    binding.buttonSearch.text = resources.getString(R.string.search_no)
//                    binding.buttonSearch.text = resources.getString(R.string.paste_number)
                    binding.editTextNumber.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun afterTextChanged(s: Editable) {

            }
        })

        binding.editTextNumber.setOnTouchListener(object : OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (event.action == MotionEvent.ACTION_UP) {
                    binding.editTextNumber.compoundDrawables
                    if (binding.editTextNumber.compoundDrawables[2] != null) {
                        if (event.rawX >= (binding.editTextNumber.right - binding.editTextNumber.compoundDrawables[2].bounds.width())) {
                            /* your action here */
                            binding.editTextNumber.setText("")
                            return true
                        }
                    }
                }
                return false
            }
        })

        //tv cancel
        binding.textViewCancel.click {
            back()
        }

        //tv clear
        binding.textViewClear.click {
            binding.editTextNumber.setText("")
        }

    }

    fun getOnlyNumbers(str: String): String {
        var str = str
        str = str.replace("[^\\d.]".toRegex(), "")
        return str
    }

    companion object {
        fun setWindowFlag(activity: Activity, bits: Int, on: Boolean) {
            val win: Window = activity.window
            val winParams: WindowManager.LayoutParams = win.attributes
            if (on) {
                winParams.flags = winParams.flags or bits
            } else {
                winParams.flags = winParams.flags and bits.inv()
            }
            win.attributes = winParams
        }
    }

    private fun listenToViewModel() {

        mViewModel.searchResultSuccessResponse.observe(this, androidx.lifecycle.Observer {
            try {
                val result = it.string()
                val searchContactRootModel = Gson().fromJson(result, SearchContactRootModel::class.java)
                searchContactDataModelList.clear()
                searchContactRootModel.data?.apply {
                    searchContactDataModelList.addAll(this)
                }
                Log.i("SearchNumberActivity", "listenToViewModel: " + searchContactRootModel.data!!.size)

                if (searchContactRootModel.data!!.size > 0) {
                    Log.i("SearchNumberActivity", "listenToViewModel: " + searchContactRootModel.data!!.get(0).id)
                    isAddNew = false
                    isDetailAPICall = true
                    pos = ((searchContactDataModelList.size) - 1)
                    CheckRecordExists().execute(pos)
                    dismissProgressDialog()
                    showInterstitial(null, this@SearchNumberActivity, object : InterfaceForClick {
                        override fun onClick(`val`: Int) {
                            startActivity(Intent(this@SearchNumberActivity, ContactDetailActivity::class.java).putExtra("id", searchContactRootModel.data?.get(0)!!.id))
                            finish()
                        }
                    })
                } else {
                    showInterstitial(null, this@SearchNumberActivity, object : InterfaceForClick {
                        override fun onClick(`val`: Int) {
                            back()
                        }
                    })

                }
//                finish()

            } catch (e: IOException) {
                e.printStackTrace()
            }
        })

        mViewModel.searchResultErrorResponse.observe(this, androidx.lifecycle.Observer {
            textViewSearch.beGone()
            linearLayoutNoMessage.gravity = Gravity.TOP
            buttonAddContact.beVisible()
            textViewNoContactMessage.beVisible()
            textViewNoContactMessage.text = resources.getString(R.string.no_conatct_yes)
            linearLayoutAddNewContact.beVisible()
        })

        mViewModel.noInternetException.observe(this, androidx.lifecycle.Observer {
            if (!InternetConnection.checkConnection(this)) {
                resources.getString(R.string.no_internet).showToast(this)
            } else {
                resources.getString(R.string.something_wrong).showToast(this)
            }
        })

    }

    @SuppressLint("StaticFieldLeak")
    inner class CheckRecordExists : AsyncTask<Int, Void, Int>() {

        override fun onPostExecute(integer: Int) {
            super.onPostExecute(integer)
            if (integer <= 0) {
                searchContactDataModelList[pos].lastAdded = Utility.getTodayDate("yyyy/MM/dd HH:mm:ss")
                searchViewModel.apply {
                    insert(searchContactDataModelList[pos])
                }
            }
        }

        override fun doInBackground(vararg params: Int?): Int {
            return searchViewModel.isExistsData(searchContactDataModelList[params[0]!!].id)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun back() {

        val intent = Intent()
        intent.putExtra("search", binding.editTextNumber.text.toString())
        setResult(Constant.SEARCH_RESULT, intent)
        finish()
    }
}