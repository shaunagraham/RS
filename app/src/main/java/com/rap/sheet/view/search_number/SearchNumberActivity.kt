package com.rap.sheet.view.search_number

import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.View.OnTouchListener
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.rap.sheet.R
import com.rap.sheet.ads.InterfaceForClick
import com.rap.sheet.ads.InterstitialAdCommon
import com.rap.sheet.ads.InterstitialAdCommon.showInterstitial
import com.rap.sheet.db.SearchViewModel
import com.rap.sheet.extenstion.*
import com.rap.sheet.model.SearchConatct.SearchContactDataModel
import com.rap.sheet.model.SearchConatct.SearchContactRootModel
import com.rap.sheet.utilitys.Constant
import com.rap.sheet.utilitys.InternetConnection
import com.rap.sheet.utilitys.Utility
import com.rap.sheet.view.common.BaseActivity
import com.rap.sheet.view.contact_detail.ContactDetailActivity
import com.rap.sheet.viewmodel.HomeViewModel
import kotlinx.android.synthetic.main.activity_search_number.*
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.fragment_search.buttonAddContact
import kotlinx.android.synthetic.main.no_message_view.*
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.IOException

class SearchNumberActivity : BaseActivity() {
    //    lateinit var binding: ActivitySearchNumberBinding
    private var isAddNew = false
    private var isDetailAPICall = false
    private var searchContactDataModelList: MutableList<SearchContactDataModel> = mutableListOf()
    private var pos = -1
    private val mViewModel: HomeViewModel by viewModel()
    private var mMixpanel: MixpanelAPI? = null
    private lateinit var searchViewModel: SearchViewModel

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_number)
        mMixpanel = MixpanelAPI.getInstance(this, Constant.MIXPANEL_API_TOKEN)
        searchViewModel = ViewModelProvider(this).get(SearchViewModel::class.java)
        InterstitialAdCommon.initInterstitial(this)
        InterstitialAdCommon.loadInterstitial()
        getIntentData()
        setView()
    }

    private fun getIntentData() {
        intent.extras?.apply {
            editTextNumber.setText(this.getString("search"))
            editTextNumber.setSelection(editTextNumber.text.toString().length)
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("ClickableViewAccessibility")
    fun setView() {
        //make translucent statusBar on kitkat devices
        if (Build.VERSION.SDK_INT in 19..20) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                window.statusBarColor = Color.TRANSPARENT
//                setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true)
            }
        }
        if (Build.VERSION.SDK_INT >= 19) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        //make fully Android Transparent Status bar
        if (Build.VERSION.SDK_INT >= 21) {
//            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
            window.statusBarColor = Color.TRANSPARENT
        }

        buttonSearch.click {
            if (buttonSearch.text.equals(resources.getString(R.string.paste_number))) {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                try {
                    val txt = clipboard.primaryClip!!.getItemAt(0).text
                    val str: String = getOnlyNumbers(txt.toString())
                    editTextNumber.setText(str)
                } catch (e: Exception) {
                }
            } else {
                launchProgressDialog()
                isAddNew = false
                isDetailAPICall = true
                listenToViewModel()
                val searchJson = JSONObject()
                searchJson.put("search_number", editTextNumber.text.toString())
                mMixpanel?.track("SearchNumber", searchJson)
                logEvent("search_number", editTextNumber.text.toString(), "SearchNumber")
                mViewModel.searchUser(editTextNumber.text.toString())
            }
        }

        //edit text
        editTextNumber.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
//                buttonSearch.text = resources.getString(R.string.search_no)
//                editTextNumber.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_close, 0)

                if (editTextNumber.text.toString().trim().isNotEmpty()) {
                    // Add drawable Right
                    buttonSearch.text = resources.getString(R.string.search)
                    editTextNumber.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_close, 0)
                } else {
//                    buttonSearch.text = resources.getString(R.string.search_no)
                    buttonSearch.text = resources.getString(R.string.paste_number)
                    editTextNumber.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun afterTextChanged(s: Editable) {

            }
        })

        editTextNumber.setOnTouchListener(object : OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (event.action == MotionEvent.ACTION_UP) {
                    editTextNumber.compoundDrawables
                    if (editTextNumber.compoundDrawables[2] != null) {
                        if (event.rawX >= (editTextNumber.right - editTextNumber.compoundDrawables[2].bounds.width())) {
                            /* your action here */
                            editTextNumber.setText("")
                            return true
                        }
                    }
                }
                return false
            }
        })

        //tv cancel
        textViewCancel.click {
            back()
        }

        //tv clear
        textViewClear.click {
            editTextNumber.setText("")
        }

    }

    private fun getOnlyNumbers(str: String): String {
        var str1 = str
        str1 = str1.replace("[^\\d.]".toRegex(), "")
        return str1
    }

    companion object {
        /*fun setWindowFlag(activity: Activity, bits: Int, on: Boolean) {
            val win: Window = activity.window
            val winParams: WindowManager.LayoutParams = win.attributes
            if (on) {
                winParams.flags = winParams.flags or bits
            } else {
                winParams.flags = winParams.flags and bits.inv()
            }
            win.attributes = winParams
        }*/
    }

    private fun listenToViewModel() {

        mViewModel.searchResultSuccessResponse.observe(this, {
            try {
                val result = it.string()
                val searchContactRootModel = Gson().fromJson(result, SearchContactRootModel::class.java)
                searchContactDataModelList.clear()
                searchContactRootModel.data?.apply {
                    searchContactDataModelList.addAll(this)
                }

                if (searchContactRootModel.data!!.size > 0) {
                    isAddNew = false
                    isDetailAPICall = true
                    pos = ((searchContactDataModelList.size) - 1)
                    checkRecordExists(pos)
//                    CheckRecordExists().execute(pos)
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

        mViewModel.searchResultErrorResponse.observe(this, {
            textViewSearch.beGone()
            linearLayoutNoMessage.gravity = Gravity.TOP
            buttonAddContact.beVisible()
            textViewNoContactMessage.beVisible()
            textViewNoContactMessage.text = resources.getString(R.string.no_conatct_yes)
            linearLayoutAddNewContact.beVisible()
        })

        mViewModel.noInternetException.observe(this, {
            if (!InternetConnection.checkConnection(this)) {
                resources.getString(R.string.no_internet).showToast(this)
            } else {
                resources.getString(R.string.something_wrong).showToast(this)
            }
        })

    }

    private fun checkRecordExists(pos: Int) {
        lifecycleScope.executeAsyncTask(onPreExecute = {

        }, doInBackground = {
            searchViewModel.isExistsData(searchContactDataModelList[pos].id)
        }, onPostExecute = {
            if (it <= 0) {
                searchContactDataModelList[pos].lastAdded = Utility.getTodayDate("yyyy/MM/dd HH:mm:ss")
                searchViewModel.apply {
                    insert(searchContactDataModelList[pos])
                }
            }
        })
    }

    /* @SuppressLint("StaticFieldLeak")
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
     }*/

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun back() {

        val intent = Intent()
        intent.putExtra("search", editTextNumber.text.toString())
        setResult(Constant.SEARCH_RESULT, intent)
        finish()
    }
}