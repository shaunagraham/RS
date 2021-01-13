package com.rap.sheet.view.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.rap.sheet.R
import com.rap.sheet.adapter.SearchContactAdapter
import com.rap.sheet.db.SearchViewModel
import com.rap.sheet.extenstion.*
import com.rap.sheet.model.SearchConatct.SearchContactDataModel
import com.rap.sheet.model.SearchConatct.SearchContactRootModel
import com.rap.sheet.utilitys.Constant
import com.rap.sheet.utilitys.InternetConnection
import com.rap.sheet.utilitys.SwipeHelper
import com.rap.sheet.utilitys.Utility
import com.rap.sheet.view.add_contact.AddContactActivity
import com.rap.sheet.view.comment.CommentActivity
import com.rap.sheet.view.common.BaseFragment
import com.rap.sheet.view.contact_detail.ContactDetailActivity
import com.rap.sheet.viewmodel.HomeViewModel
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.fragment_search.buttonAddContact
import kotlinx.android.synthetic.main.no_message_view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.IOException

class HomeFragment : BaseFragment() {

    private val mViewModel: HomeViewModel by viewModel()
    private var searchContactAdapter: SearchContactAdapter? = null
    private var searchContactDataModelList: MutableList<SearchContactDataModel> = mutableListOf()
    private var pos = -1
    private lateinit var searchViewModel: SearchViewModel
    private var itemTouchHelper: ItemTouchHelper? = null
    var isAddNew = false
    var isAPICall = false
    var isDetailAPICall = false
    var isSearch = false
    private var number: String? = null
    var search:String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        searchViewModel = ViewModelProvider(this).get(SearchViewModel::class.java)
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        listenToViewModel()
        arguments?.apply {
            search = this.getString("search")
            if (!search.isNullOrEmpty()) {
                isSearch = true
                textViewSearch.beVisible()
                recyclerViewContact.beGone()
                linearLayoutAddNewContact.beGone()
                mViewModel.searchUser(search!!)
            } else {

                Log.i("TAG", "onViewCreated: ")
                isSearch = false
            }
        }
        // mViewModel.searchUser("")
    }

    private fun listenToViewModel() {
        mViewModel.searchResultSuccessResponse.observe(viewLifecycleOwner, Observer {
            try {
                isAPICall = false
                val result = it.string()
                val searchContactRootModel = Gson().fromJson(result, SearchContactRootModel::class.java)
                searchContactDataModelList.clear()
                searchContactRootModel.data?.apply {
                    searchContactDataModelList.addAll(this)
                }
                setUpSearchContactAdapter()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        })
        mViewModel.searchResultErrorResponse.observe(viewLifecycleOwner, Observer {
            Log.d("Hello", it.string())
            textViewSearch.beGone()
            linearLayoutNoMessage.gravity = Gravity.TOP
            buttonAddContact.beVisible()
            textViewNoContactMessage.beVisible()
            textViewNoContactMessage.text = resources.getString(R.string.no_conatct_yes)
            linearLayoutAddNewContact.beVisible()
        })

        mViewModel.noInternetException.observe(viewLifecycleOwner, Observer {
            if (!InternetConnection.checkConnection(activity)) {
                resources.getString(R.string.no_internet).showToast(requireContext())
            } else {
                resources.getString(R.string.something_wrong).showToast(requireContext())
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {
        recyclerViewContact.layoutManager = LinearLayoutManager(requireContext())
        searchContactAdapter = SearchContactAdapter(searchContactDataModelList, onItemClick = ::onItemClick, onPopUpClick = ::onPopUpClick)
        recyclerViewContact.adapter = searchContactAdapter

        launchHistorySearchData()
        buttonAddContact.click {
            startActivityFromFragment<AddContactActivity>()
        }

        searchViewModel.allSearchData.observe(viewLifecycleOwner, Observer<List<SearchContactDataModel>> { searchDataModels ->
            if (searchDataModels.isNotEmpty()) {
                if (!isSearch) {
                    searchContactDataModelList.clear()
                    searchContactDataModelList.addAll(searchDataModels)
                    searchContactAdapter?.apply {
                        notifyDataSetChanged()
                    }
                    recyclerViewContact.beVisible()
                    linearLayoutAddNewContact.beGone()
                }
            } else {
                if (!isSearch) {
                    recyclerViewContact.beGone()
                    if(sharedPreferences.isFirstTime){
                        sharedPreferences.isFirstTime = false
                        linearLayoutAddNewContact.beGone()
                    }else{
                        linearLayoutAddNewContact.beVisible()
                    }
                    textViewSearch.beGone()
                    textViewNoContactMessage.text = resources.getString(R.string.no_history_yet)
                }
            }
        })
    }

    private fun onPopUpClick(position: Int, view: View) {
        val popup = PopupMenu(requireActivity(), view, Gravity.END)
        //inflating menu from xml resource
        popup.inflate(R.menu.pop_up)
        //adding click listener
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.navigation_call -> {
                    number = searchContactDataModelList[position].number
                    val permissionCheck = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE)
                    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(arrayOf(Manifest.permission.CALL_PHONE), 103)
                    } else {
                        Utility.callUserCall(activity, number)
                    }
                    true
                }
                R.id.navigation_message -> {
                    //Redirect system Message
                    Utility.callUserSMS(activity, searchContactDataModelList[position].number)
                    true
                }
                R.id.navigation_save -> {
                    //Save Contact to contact list
                    Utility.saveNewNumber(activity, searchContactDataModelList[position].number, searchContactDataModelList[position].firstName + " " + searchContactDataModelList[position].lastName)
                    true
                }
                R.id.navigation_report -> {
                    val bundle = Bundle()
                    bundle.putString("id", searchContactDataModelList[position].id)
                    startActivityFromFragment<CommentActivity>(bundle)

                    true
                }
                else -> false
            }
        }
        //displaying the popup
        popup.show()
    }

    private fun onItemClick(position: Int) {
        CheckRecordExists().execute(position)
        isAddNew = false
        isDetailAPICall = true
        pos = position
        val bundle = Bundle()
        bundle.putString("id", searchContactDataModelList[position].id)
        startActivityFromFragment<ContactDetailActivity>(bundle)
    }

    private fun launchHistorySearchData() {
        itemTouchHelper = ItemTouchHelper(object : SwipeHelper(activity, recyclerViewContact) {
            override fun instantiateUnderlayButton(viewHolder: RecyclerView.ViewHolder?,
                                                   underlayButtons: MutableList<UnderlayButton>?) {
                underlayButtons!!.add(UnderlayButton(
                        "",
                        R.drawable.delete3,
                        Color.parseColor("#000000"),
                        true,
                        object : UnderlayButtonClickListener {
                            override fun onClick(pos: Int) {
                                val name = searchContactDataModelList[pos].firstName + " " + searchContactDataModelList[pos].lastName
                                // backup of removed item for undo purpose
                                val deletedItem = searchContactDataModelList[pos]
                                val deletedIndex = pos

                                // remove the item from recycler view
                                searchViewModel.apply {
                                    delete(searchContactDataModelList[pos])
                                }
                                searchContactAdapter?.apply {
                                    removeItem(pos)
                                }

                                requireActivity().makeSnackBar(coordinatorLayoutRoot, "$name removed from history!", "UNDO", View.OnClickListener {
                                    searchContactAdapter?.apply {
                                        restoreItem(deletedItem, deletedIndex)
                                    }
                                    searchViewModel.apply {
                                        insert(deletedItem)
                                    }

                                })

                            }
                        }
                ))
            }
        })
        itemTouchHelper?.attachToRecyclerView(recyclerViewContact)
        linearLayoutNoMessage.beGone()
    }


    private fun setUpSearchContactAdapter() {
        searchContactAdapter?.apply {
            notifyDataSetChanged()
        }

        textViewSearch.beGone()
        Log.d("Hello", searchContactDataModelList.size.toString())
        if (!searchContactDataModelList.isNullOrEmpty()) {
            linearLayoutNoMessage.beGone()
            recyclerViewContact.beVisible()
            linearLayoutAddNewContact.beGone()
        } else {
            textViewMessage.beVisible()
            recyclerViewContact.beGone()
            buttonAddContact.beVisible()
            textViewNoContactMessage.beVisible()
//            textViewNoContactMessage.text = resources.getString(R.string.no_conatct_yes)
            val stringBuilderPhone = StringBuilder(search!!.replace("-".toRegex(), ""))
            if (stringBuilderPhone.length > 3) {

                stringBuilderPhone.insert(3, "-")
            }
            if (stringBuilderPhone.length > 7) {
                stringBuilderPhone.insert(7, "-")
            }
            textViewNoContactMessage.text = resources.getString(R.string.no_result_for)+" "+stringBuilderPhone.toString()
            linearLayoutAddNewContact.beVisible()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Utility.callUserCall(requireContext(), number)
        } else {
            Log.d("TAG", "Call Permission Not Granted")
        }
    }

//
//    @SuppressLint("ClickableViewAccessibility")
//    private fun performSearchButtonClick(editTextSearch: EditText) {
//        val `in` = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        `in`.hideSoftInputFromWindow(editTextSearch.windowToken, 0)
//        textViewSearch!!.visibility = View.VISIBLE
//        linearLayoutNoMessage!!.visibility = View.GONE
//        recyclerViewContact!!.visibility = View.GONE
//        textViewNoContactMessage!!.visibility = View.GONE
//        //        if (!BaseApplication.adsRemovePref.isAdsRemove()) {
////            loadInterstitialAds();
////        }
//        if (itemTouchHelper != null) {
//            itemTouchHelper!!.attachToRecyclerView(null)
//            itemTouchHelper = ItemTouchHelper(null)
//            itemTouchHelper = null
//        }
//        if (editTextSearch.text.toString().trim { it <= ' ' }.isEmpty()) {
//            isSearch = false
//            launchHistorySearchData()
//        } else {
//            isSearch = true
//            relativeLayoutContactList!!.visibility = View.VISIBLE
//            if (isAPICall) {
//                searchContacts!!.cancel()
//            }
//            isAPICall = true
//            mViewModel.searchUser(editTextSearch.text.toString().trim { it <= ' ' })
//
//        }
//    }


//    inner class SearchContactTextWatcher : TextWatcher {
//        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
//        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
//            if (editTextSearch!!.text.toString().trim { it <= ' ' }.isEmpty()) {
//                isSearch = false
//                launchHistorySearchData()
//            } else {
//                assert(fragmentManager != null)
//                if (fragmentManager!!.backStackEntryCount > 1) {
//                    fragmentManager!!.popBackStack()
//                }
//            }
//        }
//
//        override fun afterTextChanged(editable: Editable) {}
//    }

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

//    private val linearLayoutAddNewContactClickListener = View.OnClickListener {
//        val intent = Intent(activity, AddContactActivity::class.java)
//        startActivityForResult(intent, Constant.UPDATE_COMMENT)
//    }
//    private val buttonAddContactClickListener = View.OnClickListener {
//        val intent = Intent(activity, AddContactActivity::class.java)
//        startActivityForResult(intent, Constant.UPDATE_COMMENT)
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constant.UPDATE_COMMENT) {
            if (data != null) {
                if (data.getStringExtra("id") != null) {
                    isAddNew = true
                    val bundle = Bundle()
                    bundle.putString("id", data.getStringExtra("id"))
                    startActivityFromFragment<ContactDetailActivity>(bundle)
                }
            }
        }
    }


//    private fun callReportAPI(position: Int, title: String, comment: String) {
//        restInterface!!.reportContact(BaseApplication.Companion.userPref.getUserID(),
//                searchContactDataModelList!![position].id,
//                title,
//                comment
//        )
//                .enqueue(reportContactsCallBack)
//    }

//    private val reportContactsCallBack: Callback<ResponseBody?> = object : Callback<ResponseBody> {
//        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
//            if (response.code() == 201) {
//                try {
//                    val jsonObjectData = JSONObject(response.body()!!.string())
//                    val jsonObjectMessage = jsonObjectData.getJSONObject("data")
//                    Utility.makeAlertDialog(activity, true, resources.getString(R.string.thank_you), jsonObjectMessage.getString("message"), resources.getString(android.R.string.ok), "", DialogInterface.OnClickListener { dialog, which -> dialog.cancel() }, null)
//                } catch (e: JSONException) {
//                    e.printStackTrace()
//                } catch (e: IOException) {
//                    e.printStackTrace()
//                }
//            }
//            alertDialogReport!!.cancel()
//            Utility.dismissProgressDialog()
//        }
//
//        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//            Log.d("Hello", t.message)
//            alertDialogReport!!.cancel()
//            Utility.dismissProgressDialog()
//        }
//    }
//    private val searchContactsCallBack: Callback<ResponseBody?> = object : Callback<ResponseBody> {
//        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
//            Log.d("Hello", response.code().toString() + "")
//            if (response.code() == Constant.SUCCESS_STATUS) {
//                try {
//                    isAPICall = false
//                    val result = response.body()!!.string()
//                    Log.d("Hello", result)
//                    var searchContactRootModel = SearchContactRootModel()
//                    searchContactRootModel = Gson().fromJson(result, SearchContactRootModel::class.java)
//                    searchContactDataModelList!!.clear()
//                    searchContactDataModelList.addAll(searchContactRootModel.data)
//                    setUpSearchContactAdapter()
//                } catch (e: IOException) {
//                    e.printStackTrace()
//                }
//            } else {
//                textViewSearch!!.visibility = View.GONE
//                linearLayoutNoMessage!!.gravity = Gravity.TOP
//                buttonAddContact!!.visibility = View.VISIBLE
//                textViewMessage!!.visibility = View.VISIBLE
//                textViewMessage!!.text = resources.getString(R.string.no_conatct_yes)
//                linearLayoutNoMessage!!.visibility = View.VISIBLE
//            }
//        }
//
//        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//            if (!InternetConnection.checkConnection(activity)) {
//                Toast.makeText(activity, resources.getString(R.string.no_internet), Toast.LENGTH_SHORT).show()
//            }
//        }
//    }

    companion object {
        fun newInstance(search: String): Fragment {
            val args = Bundle()
            args.putString("search", search)
            val fragment = HomeFragment()
            fragment.arguments = args
            return fragment
        }
    }
}