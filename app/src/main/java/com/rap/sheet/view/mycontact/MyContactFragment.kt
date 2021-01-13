package com.rap.sheet.view.mycontact

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.IntentFilter
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.rap.sheet.R
import com.rap.sheet.adapter.MyContactAdapter
import com.rap.sheet.db.SearchViewModel
import com.rap.sheet.extenstion.*
import com.rap.sheet.model.MyContact.ContactModel
import com.rap.sheet.model.MyContact.MyContactRootModel
import com.rap.sheet.receiver.AddNewContactReceiver
import com.rap.sheet.receiver.AddNewContactReceiver.OnNewContactListener
import com.rap.sheet.utilitys.Constant
import com.rap.sheet.utilitys.SwipeHelper
import com.rap.sheet.view.add_contact.AddContactActivity
import com.rap.sheet.view.comment.CommentActivity
import com.rap.sheet.view.common.BaseFragment
import com.rap.sheet.viewmodel.MyContactViewModel
import kotlinx.android.synthetic.main.activity_my_contact.*
import kotlinx.android.synthetic.main.no_internet_view.*
import kotlinx.android.synthetic.main.no_message_view.*
import kotlinx.android.synthetic.main.progress_dialog_view.*
import org.json.JSONException
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by mvayak on 02-08-2018.
 */

class MyContactFragment : BaseFragment(), OnNewContactListener {
    private val mViewModel: MyContactViewModel by viewModel()

    private var contactModelList: MutableList<ContactModel> = mutableListOf()
    private var myContactAdapter: MyContactAdapter? = null

    //    private LinearLayout linearLayoutAddNewContact;
    private var position: Int = 0

    private lateinit var searchViewModel: SearchViewModel

    private var addNewContactReceiver: AddNewContactReceiver? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_my_contact, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addNewContactReceiver = AddNewContactReceiver(this)
        //  restInterface = RetrofitClient.getClient(BuildConfig.APIPATH).create(RestInterface::class.java)

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(addNewContactReceiver!!, IntentFilter(Constant.NEW_CONTACTS))
        searchViewModel = ViewModelProvider(this).get(SearchViewModel::class.java)

        recyclerViewMyContact.layoutManager = LinearLayoutManager(requireContext())
        myContactAdapter = MyContactAdapter(contactModelList, onItemClick = ::onContactClick)
        recyclerViewMyContact.adapter = myContactAdapter
        //linearLayoutAddNewContact.setVisibility(View.GONE);
        buttonTryAgain.click {
            linearLayoutProgressBar.beVisible()
            linearLayoutNoMessage.beGone()
            constraintLayoutNoInternet.beGone()
            recyclerViewMyContact.beVisible()
            mViewModel.getContactList(sharedPreferences.userID)
        }
        listenToViewModel()
        buttonContact.click {
            val bundle = Bundle()
            bundle.putBoolean("myContact", true)
            startActivityFromFragment<AddContactActivity>(bundle)
            // startActivity(Intent(mainActivity, AddContactActivity::class.java).putExtra("myContact", true))
        }

//        RecyclerItemTouchHelperContact recyclerItemTouchHelperSearch = new RecyclerItemTouchHelperContact(ItemTouchHelper.LEFT, ItemTouchHelper.LEFT, this);
        val itemTouchHelper = ItemTouchHelper(object : SwipeHelper(requireContext(), recyclerViewMyContact) {
            override fun instantiateUnderlayButton(viewHolder: RecyclerView.ViewHolder?, underlayButtons: MutableList<UnderlayButton>?) {
                underlayButtons!!.add(UnderlayButton(
                        "",
                        R.drawable.delete3,
                        Color.parseColor("#000000"),
                        true,
                        object : UnderlayButtonClickListener {
                            override fun onClick(pos: Int) {
                                requireActivity().displayAlertDialog(desc = resources.getString(R.string.delete_contact_msg), cancelable = false,
                                        positiveText = resources.getString(R.string.yes), positiveClick = DialogInterface.OnClickListener { dialog, _ ->
                                    dialog?.apply {
                                        this.dismiss()
                                        callDeleteMyContact(pos)
                                    }
                                }, negativeText = resources.getString(R.string.cancel), negativeClick = DialogInterface.OnClickListener { dialog, _ ->
                                    dialog?.apply {
                                        this.dismiss()
                                        myContactAdapter?.apply {
                                            notifyItemChanged(pos)
                                        }
                                    }
                                })
                            }
                        }
                ))
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerViewMyContact)
//        myContactAdapter!!.itemClickListener(myContactItemClickListener)

        //linearLayoutAddNewContact.setOnClickListener(buttonAddContactClickListener);

//        buttonAddContact.setOnClickListener(buttonAddContactClickListener)
        mViewModel.getContactList(sharedPreferences.userID)

//        buttonAddContact.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(mainActivity, AddNumberActivity.class);
//                startActivityForResult(intent, Constant.UPDATE_COMMENT);
//            }
//        });

    }

    private fun onContactClick(position: Int) {
        this.position = position
        val bundle = Bundle()
        bundle.putString("id", contactModelList[position].id)
        startActivityFromFragment<CommentActivity>(bundle)
    }

    private fun listenToViewModel() {
        mViewModel.deleteContactSuccessResponse.observe(viewLifecycleOwner, Observer {
            checkRecordExists().execute(position)
        })
        mViewModel.deleteContactErrorResponse.observe(viewLifecycleOwner, Observer {
            dismissProgressDialog()
        })

        mViewModel.noInternetException.observe(viewLifecycleOwner, Observer {

        })

        mViewModel.myContactListSuccessResponse.observe(viewLifecycleOwner, Observer {
            contactModelList.clear()
            val result: String = it.string()
            val myContactRootModel = Gson().fromJson(result, MyContactRootModel::class.java)
            myContactRootModel?.contacts?.apply {
                contactModelList.addAll(this)
                myContactAdapter?.apply {
                    notifyDataSetChanged()
                }
            }


            checkContactAvailableOrNot()
        })
        mViewModel.myContactListErrorResponse.observe(viewLifecycleOwner, Observer {

        })
    }

    //    private void checkAdRemoveOrNot() {
    //        if (!BaseApplication.adsRemovePref.isAdsRemove()) {
    //            View adContainer = view.findViewById(R.id.adViewSearch);
    //            adViewSearch = new AdView(mainActivity);
    //            adViewSearch.setAdSize(AdSize.SMART_BANNER);
    //            adViewSearch.setAdUnitId(BuildConfig.BANNER_ID);
    //            ((RelativeLayout) adContainer).addView(adViewSearch);
    //            loadBannerAds();
    //        }
    //    }
    //
    //    private void loadBannerAds() {
    //        AdRequest adRequest = new AdRequest.Builder().addTestDevice("8E58FF204E99652B37212707E7509C5C").build();
    //        adViewSearch.loadAd(adRequest);
    //        adViewSearch.setAdListener(new AdListener() {
    //            @Override
    //            public void onAdLoaded() {
    //                // Code to be executed when an ad finishes loading.
    //            }
    //
    //            @Override
    //            public void onAdFailedToLoad(int errorCode) {
    //                // Code to be executed when an ad request fails.
    //                // loadBannerAds();
    //               ;
    //            }
    //
    //            @Override
    //            public void onAdOpened() {
    //                // Code to be executed when an ad opens an overlay that
    //                // covers the screen.
    //            }
    //
    //            @Override
    //            public void onAdLeftApplication() {
    //                // Code to be executed when the user has left the app.
    //            }
    //
    //            @Override
    //            public void onAdClosed() {
    //                // Code to be executed when when the user is about to return
    //                // to the app after tapping on an ad.
    //            }
    //        });
    //    }
    // For get All My Contact
//    val allMyContact: Unit
//        get() {
//            val getAllMyContact: Call<ResponseBody?>? = restInterface!!.getAllMyContact(BaseApplication.Companion.userPref.getUserID())
//            getAllMyContact!!.enqueue(allMyContactCallBack)
//        }
//
//    private val allMyContactCallBack: Callback<ResponseBody?> = object : Callback<ResponseBody> {
//        public override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
//            if (response.code() == Constant.SUCCESS_STATUS) {
//                try {
//                    contactModelList!!.clear()
//                    val result: String = response.body()!!.string()
//                    var myContactRootModel: MyContactRootModel = MyContactRootModel()
//                    myContactRootModel = Gson().fromJson(result, MyContactRootModel::class.java)
//                    contactModelList.addAll(myContactRootModel.getContacts())
//                    myContactAdapter!!.notifyDataSetChanged()
//                    checkContactAvailableOrNot()
//                } catch (e: IOException) {
//                    e.printStackTrace()
//                }
//            } else {
//                reLoadAPI()
//            }
//            linearLayoutProgressBar!!.setVisibility(View.GONE)
//        }
//
//        public override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//            linearLayoutProgressBar!!.setVisibility(View.GONE)
//            if (!InternetConnection.checkConnection(mainActivity)) {
//                constraintLayoutNoInternet!!.setVisibility(View.VISIBLE)
//            } else {
//                reLoadAPI()
//            }
//        }
//    }
//
//    fun reLoadAPI() {
//        Utility.makeAlertDialog(mainActivity, false, "", getResources().getString(R.string.something_wrong), getResources().getString(R.string.retry), getResources().getString(R.string.cancel), object : DialogInterface.OnClickListener {
//            public override fun onClick(dialog: DialogInterface, which: Int) {
//                dialog.dismiss()
//                linearLayoutProgressBar!!.setVisibility(View.VISIBLE)
//                allMyContact
//            }
//        }, object : DialogInterface.OnClickListener {
//            public override fun onClick(dialog: DialogInterface, which: Int) {
//                dialog.dismiss()
//            }
//        })
//    }

    private fun checkContactAvailableOrNot() {
        linearLayoutProgressBar.beGone()
        if (!contactModelList.isNullOrEmpty()) {
            linearLayoutNoMessage.beGone()
            recyclerViewMyContact.beVisible()
        } else {
            recyclerViewMyContact.beGone()
            linearLayoutNoMessage.beVisible()
        }
    }

    private fun callDeleteMyContact(position: Int) {
        this.position = position
        launchProgressDialog()
        mViewModel.deleteContact(contactModelList[position].id.toString(), contactModelList[position].userId.toString())
    }

//    private val myContactItemClickListener: RecyclerViewItemClickInterface = object : RecyclerViewItemClickInterface {
//        public override fun onItemClick(pos: Int, view: View?) {
//            position = pos
//            val intentComment: Intent = Intent(mainActivity, CommentActivity::class.java)
//            intentComment.putExtra("id", contactModelList!!.get(pos).getId())
//            startActivity(intentComment)
//        }
//    }

    override fun onNewContactReady(contact: String?) {
        try {
            val jsonObjectContact = JSONObject(contact)
            val contactModel = ContactModel()
            contactModel.email = jsonObjectContact.getString("email")
            contactModel.firstName = jsonObjectContact.getString("first_name")
            contactModel.number = jsonObjectContact.getString("number")
            contactModel.lastName = jsonObjectContact.getString("last_name")
            contactModel.id = jsonObjectContact.getString("id")
            contactModel.userId = jsonObjectContact.getString("user_id")
            contactModel.avgRate = "0"
            contactModelList.add(contactModel)
            myContactAdapter?.apply {
                notifyDataSetChanged()
            }
            checkContactAvailableOrNot()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    @SuppressLint("StaticFieldLeak")
    inner class checkRecordExists : AsyncTask<Int?, Void?, Int>() {

        override fun onPostExecute(integer: Int) {
            super.onPostExecute(integer)
            if (integer > 0) {
                searchViewModel.apply {
                    deleteSingleRecord(contactModelList[position].id)
                }
            }
            contactModelList.removeAt(position)
            myContactAdapter!!.notifyItemRemoved(position)
            checkContactAvailableOrNot()
            dismissProgressDialog()
        }

        override fun doInBackground(vararg params: Int?): Int {
            return searchViewModel.isExistsData(contactModelList[position].id)
        }
    }

//    private val buttonAddContactClickListener: View.OnClickListener = object : View.OnClickListener {
//        public override fun onClick(v: View) {
//            Toast.makeText(mainActivity, "dfd", Toast.LENGTH_SHORT).show()
//            startActivity(Intent(mainActivity, AddContactActivity::class.java).putExtra("myContact", true))
//        }
//    }

    override fun onDestroy() {
        super.onDestroy()
        addNewContactReceiver?.apply {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(this)
        }

        mViewModel.onDetach()
    }

    companion object {
        fun newInstance(): MyContactFragment {
            return MyContactFragment()
        }
    }

}