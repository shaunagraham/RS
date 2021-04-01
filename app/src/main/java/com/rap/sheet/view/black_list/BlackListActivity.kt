package com.rap.sheet.view.black_list

import android.content.IntentFilter
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.rap.sheet.R
import com.rap.sheet.adapter.MyContactAdapter
import com.rap.sheet.extenstion.*
import com.rap.sheet.model.MyContact.ContactModel
import com.rap.sheet.model.MyContact.MyContactRootModel
import com.rap.sheet.receiver.BrowseContactReceiver
import com.rap.sheet.utilitys.Constant
import com.rap.sheet.utilitys.InternetConnection
import com.rap.sheet.view.common.BaseActivity
import com.rap.sheet.view.contact_detail.ContactDetailActivity
import com.rap.sheet.viewmodel.BlackListViewModel
import kotlinx.android.synthetic.main.activity_black_list.*
import kotlinx.android.synthetic.main.activity_my_contact.*
import kotlinx.android.synthetic.main.activity_my_contact.linearLayoutSearch
import kotlinx.android.synthetic.main.activity_my_contact.recyclerViewMyContact
import kotlinx.android.synthetic.main.no_black_conatct.*
import kotlinx.android.synthetic.main.no_internet_view.*
import kotlinx.android.synthetic.main.no_message_view.*
import kotlinx.android.synthetic.main.progress_dialog_view.*
import kotlinx.android.synthetic.main.top_view_layout.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class BlackListActivity : BaseActivity(), BrowseContactReceiver.RefreshContactList {
    private var contactModelList: MutableList<ContactModel> = mutableListOf()
    private var myContactAdapter: MyContactAdapter? = null
    private val mViewModel: BlackListViewModel by viewModel()
    private var browseContactReceiver: BrowseContactReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_black_list)

        browseContactReceiver = BrowseContactReceiver(this)
        LocalBroadcastManager.getInstance(this).registerReceiver(browseContactReceiver!!, IntentFilter(Constant.REFRESH_CONTACTS))
        setUpToolBar()
        initView()
        listenToViewModel()
    }

    private fun setUpToolBar() {
        toolbar.imgClose.setOnClickListener { onBackPressed() }
        toolbar.tvTitle.text = resources.getString(R.string.blocked_calls)
    }

    /**
     * @desc receive the events
     * Also, call browseContact .
     */
    override fun onReceive(isRefresh: Boolean?) {
        if (isRefresh != null && isRefresh) {
            linearLayoutProgressBar.beVisible()
            linearLayoutNoBlack.beGone()
            constraintLayoutNoInternet.beGone()
            recyclerViewMyContact.beVisible()
            contactModelList.clear()
            myContactAdapter?.apply {
                notifyDataSetChanged()
            }
            allBrowseContactAPI()
        }
    }

    /**
     * @desc listen observer and receive the events
     * Also, Manage success and failure responces from API, Internet and un authorization.
     */
    private fun listenToViewModel() {
        mViewModel.blackListContactSuccessResponse.observe(this, {
            linearLayoutProgressBar.beGone()
            contactModelList.clear()
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
        mViewModel.blackListContactErrorResponse.observe(this, {
            linearLayoutProgressBar.beGone()
            if (!InternetConnection.checkConnection(this)) {
                constraintLayoutNoInternet.beGone()
            } else {
                reLoadAPI()
            }
        })
        mViewModel.noInternetException.observe(this, {
            dismissProgressDialog()
            if (InternetConnection.checkConnection(this)) {
                this.displayAlertDialog(
                        desc = resources.getString(R.string.something_wrong),
                        cancelable = false,
                        positiveText = resources.getString(android.R.string.ok),
                        positiveClick = { dialog, _ ->
                            dialog?.apply {
                                this.dismiss()
                            }
                        }
                )
            } else {
                this.displayAlertDialog(
                        desc = resources.getString(R.string.no_internet_msg),
                        cancelable = false,
                        positiveText = resources.getString(android.R.string.ok),
                        positiveClick = { dialog, _ ->
                            dialog?.apply {
                                this.dismiss()

                            }
                        }
                )
            }
        })
    }

    /**
     * @desc this method is used for initialize fragments, adapter and recyclerview
     */
    private fun initView() {

        allBrowseContactAPI()
        linearLayoutSearch.beGone()
        // SetUp Adapter
        recyclerViewMyContact.layoutManager = LinearLayoutManager(this)
        myContactAdapter = MyContactAdapter(contactModelList, onItemClick = ::onItemClick)
        recyclerViewMyContact.adapter = myContactAdapter

        buttonTryAgain.click {
            linearLayoutProgressBar.beVisible()
            linearLayoutNoBlack.beGone()
            constraintLayoutNoInternet.beGone()
            recyclerViewMyContact.beVisible()
            allBrowseContactAPI()
        }

    }

    /**
     * @desc this method is used for contactlist itemClick
     */
    private fun onItemClick(position: Int) {
        val bundle = Bundle()
        bundle.putString("id", contactModelList[position].id)
        startActivityInline<ContactDetailActivity>(bundle)
    }

    // For get All My Contact
    private fun allBrowseContactAPI() {
        contactModelList.clear()
        mViewModel.getBlackListContacts()
    }

    /**
     * @desc this method is used for realod allcontact
     */
    private fun reLoadAPI() {
        displayAlertDialog(
                desc = resources.getString(R.string.something_wrong),
                positiveText = resources.getString(R.string.retry),
                positiveClick = { dialog, _ ->
                    dialog?.apply {
                        dismiss()
                        linearLayoutProgressBar.beVisible()
                        allBrowseContactAPI()
                    }
                },
                negativeText = resources.getString(R.string.cancel),
                negativeClick = { dialog, _ ->
                    dialog?.apply {
                        dismiss()
                    }
                }
        )
    }

    /**
     * @desc this method is used for mange hide/show UI
     */
    private fun checkContactAvailableOrNot() {
        if (!contactModelList.isNullOrEmpty()) {
            linearLayoutNoBlack.beGone()
            recyclerViewMyContact.beVisible()
        } else {
            recyclerViewMyContact.beGone()
            linearLayoutNoBlack.beVisible()
        }
    }

    /**
     * @desc stop apis call and clear view model
     */
    override fun onDestroy() {
        super.onDestroy()
        browseContactReceiver?.apply {
            LocalBroadcastManager.getInstance(this@BlackListActivity).unregisterReceiver(this)
        }
        mViewModel.onDetach()
    }
}