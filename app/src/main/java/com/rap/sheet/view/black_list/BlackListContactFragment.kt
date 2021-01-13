package com.rap.sheet.view.black_list

import android.content.DialogInterface
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.rap.sheet.BuildConfig
import com.rap.sheet.R
import com.rap.sheet.adapter.MyContactAdapter
import com.rap.sheet.extenstion.*
import com.rap.sheet.model.MyContact.ContactModel
import com.rap.sheet.model.MyContact.MyContactRootModel
import com.rap.sheet.receiver.BrowseContactReceiver
import com.rap.sheet.receiver.BrowseContactReceiver.RefreshContactList
import com.rap.sheet.retrofit.RestInterface
import com.rap.sheet.retrofit.RetrofitClient
import com.rap.sheet.utilitys.Constant
import com.rap.sheet.utilitys.InternetConnection
import com.rap.sheet.view.add_contact.AddContactActivity
import com.rap.sheet.view.common.BaseFragment
import com.rap.sheet.view.contact_detail.ContactDetailActivity
import com.rap.sheet.viewmodel.BlackListViewModel
import kotlinx.android.synthetic.main.activity_my_contact.*

import kotlinx.android.synthetic.main.no_internet_view.*
import kotlinx.android.synthetic.main.no_message_view.*
import kotlinx.android.synthetic.main.progress_dialog_view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by mvayak on 02-08-2018.
 */

class BlackListContactFragment : BaseFragment(), RefreshContactList {
    private var restInterface: RestInterface? = null
    private var contactModelList: MutableList<ContactModel> = mutableListOf()
    private var myContactAdapter: MyContactAdapter? = null
    private val mViewModel: BlackListViewModel by viewModel()
    private var browseContactReceiver: BrowseContactReceiver? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        restInterface = RetrofitClient.getClient(BuildConfig.APIPATH).create(RestInterface::class.java)
        return inflater.inflate(R.layout.activity_my_contact, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        browseContactReceiver = BrowseContactReceiver(this)
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(browseContactReceiver!!, IntentFilter(Constant.REFRESH_CONTACTS))
        initView()
        listenToViewModel()
    }

    private fun listenToViewModel() {
        mViewModel.blackListContactSuccessResponse.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            linearLayoutProgressBar.beGone()
            val result: String = it.string()
            val myContactRootModel = Gson().fromJson(result, MyContactRootModel::class.java)
            myContactRootModel?.contacts?.apply {
                contactModelList.addAll(this)
                myContactAdapter?.apply {
                    notifyDataSetChanged()
                }
            }
            myContactAdapter?.apply {
                notifyDataSetChanged()
            }
            checkContactAvailableOrNot()
        })
        mViewModel.blackListContactErrorResponse.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            linearLayoutProgressBar.beGone()
            if (!InternetConnection.checkConnection(requireContext())) {
                constraintLayoutNoInternet.beGone()
            } else {
                reLoadAPI()
            }
        })
        mViewModel.noInternetException.observe(viewLifecycleOwner, androidx.lifecycle.Observer {

        })
    }

    // Initialling view

    private fun initView() {

        allBrowseContactAPI()
        linearLayoutSearch.beGone()
        // SetUp Adapter
        recyclerViewMyContact.layoutManager = LinearLayoutManager(requireContext())
        myContactAdapter = MyContactAdapter(contactModelList, onItemClick = ::onItemClick)
        recyclerViewMyContact.adapter = myContactAdapter

//        editTextSearch.setOnEditorActionListener(object : OnEditorActionListener {
//            override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent): Boolean {
//                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
//                    performSearchButtonClick()
//                    return true
//                }
//                return false
//            }
//        })

        buttonAddContact.click {
            startActivityFromFragmentWithBundleCode<AddContactActivity>(resultCode = Constant.UPDATE_COMMENT)
        }
        buttonTryAgain.click {
            linearLayoutProgressBar.beVisible()
            linearLayoutNoMessage.beGone()
            constraintLayoutNoInternet.beGone()
            recyclerViewMyContact.beVisible()
            allBrowseContactAPI()
        }
//        editTextSearch.addTextChangedListener(SearchContactTextWatcher())

    }

    private fun onItemClick(position: Int) {
//        editTextSearch.clearFocus()
//        requireActivity().hideKeyboard()
        val bundle = Bundle()
        bundle.putString("id", contactModelList[position].id)
        startActivityFromFragment<ContactDetailActivity>(bundle)
    }

//    private fun performSearchButtonClick() {
//        requireActivity().hideKeyboard()
////        myContactAdapter.getFilter().filter(editTextSearch!!.getText().toString())
//    }

    // For get All My Contact
    private fun allBrowseContactAPI() {
        contactModelList.clear()
        mViewModel.getBlackListContacts()
    }


    private fun reLoadAPI() {
        requireActivity().displayAlertDialog(desc = resources.getString(R.string.something_wrong), positiveText = resources.getString(R.string.retry), positiveClick = DialogInterface.OnClickListener { dialog, which ->
            dialog?.apply {
                dismiss()
                linearLayoutProgressBar.beVisible()
                allBrowseContactAPI()
            }
        }, negativeText = resources.getString(R.string.cancel), negativeClick = DialogInterface.OnClickListener { dialog, which ->
            dialog?.apply {
                dismiss()
            }
        })
    }

    private fun checkContactAvailableOrNot() {
        if (!contactModelList.isNullOrEmpty()) {
            linearLayoutNoMessage.beGone()
            recyclerViewMyContact.beVisible()
            buttonAddContact.beGone()
        } else {
            recyclerViewMyContact.beGone()
            linearLayoutNoMessage.beVisible()
            buttonAddContact.beVisible()
        }
    }

    override fun onReceive(refresh: Boolean?) {
        if (refresh != null && refresh) {
            linearLayoutProgressBar.beVisible()
            linearLayoutNoMessage.beGone()
            constraintLayoutNoInternet.beGone()
            recyclerViewMyContact.beVisible()
            contactModelList.clear()
            myContactAdapter?.apply {
                notifyDataSetChanged()
            }
            allBrowseContactAPI()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        browseContactReceiver?.apply {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(this)
        }
        mViewModel.onDetach()
    }

//    inner class SearchContactTextWatcher constructor() : TextWatcher {
//        public override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
//        public override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
//            myContactAdapter!!.getFilter().filter(charSequence)
//        }
//
//        public override fun afterTextChanged(editable: Editable) {}
//    }

}