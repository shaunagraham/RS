package com.rap.sheet.view.my_contact


import android.content.DialogInterface
import android.os.Bundle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.rap.sheet.R
import com.rap.sheet.adapter.MyContactAdapter
import com.rap.sheet.extenstion.beGone
import com.rap.sheet.extenstion.displayAlertDialog
import com.rap.sheet.extenstion.userID
import com.rap.sheet.model.MyContact.ContactModel
import com.rap.sheet.model.MyContact.MyContactRootModel
import com.rap.sheet.utilitys.RecyclerItemTouchHelperContact
import com.rap.sheet.view.common.BaseActivity
import com.rap.sheet.viewmodel.MyContactViewModel
import kotlinx.android.synthetic.main.activity_my_contact.*
import kotlinx.android.synthetic.main.progress_dialog_view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class MyContactActivity : BaseActivity(), RecyclerItemTouchHelperContact.RecyclerItemTouchHelperListener {
    private val mViewModel: MyContactViewModel by viewModel()

    private var contactModelList: MutableList<ContactModel> = mutableListOf()
    private var myContactAdapter: MyContactAdapter? = null
    private var position: Int = 0
    private var itemTouchHelper: ItemTouchHelper? = null
    private var recyclerItemTouchHelperSearch: RecyclerItemTouchHelperContact? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_contact)

        contactModelList = ArrayList()
        initView()

    }

    // Initialling view
    private fun initView() {
        recyclerViewMyContact.layoutManager = LinearLayoutManager(this)
        myContactAdapter = MyContactAdapter(contactModelList)
        recyclerViewMyContact.adapter = myContactAdapter
        recyclerItemTouchHelperSearch = RecyclerItemTouchHelperContact(ItemTouchHelper.LEFT, ItemTouchHelper.LEFT, this@MyContactActivity)
        itemTouchHelper = ItemTouchHelper(recyclerItemTouchHelperSearch!!)
        itemTouchHelper!!.attachToRecyclerView(recyclerViewMyContact)
        mViewModel.getContactList(sharedPreferences.userID)
        listenToViewModel()
    }


    override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int, position: Int) {
        displayAlertDialog(desc = resources.getString(R.string.delete_contact_msg), cancelable = false,
                positiveText = resources.getString(R.string.yes), positiveClick = DialogInterface.OnClickListener { dialog, _ ->
            dialog?.apply {
                this.dismiss()
                callDeleteMyContact(position)
            }
        }, negativeText = resources.getString(R.string.cancel), negativeClick = DialogInterface.OnClickListener { dialog, _ ->
            dialog?.apply {
                this.dismiss()
                myContactAdapter?.apply {
                    notifyItemChanged(position)
                }
            }
        })

    }

    private fun callDeleteMyContact(position: Int) {
        this.position = position
        launchProgressDialog()
        mViewModel.deleteContact(contactModelList[position].id.toString(), contactModelList[position].userId.toString())

    }


    private fun listenToViewModel() {
        mViewModel.deleteContactSuccessResponse.observe(this, androidx.lifecycle.Observer {
            contactModelList.removeAt(position)
            myContactAdapter?.apply {
                notifyItemRemoved(position)
            }
            dismissProgressDialog()
        })
        mViewModel.deleteContactErrorResponse.observe(this, androidx.lifecycle.Observer {
            dismissProgressDialog()
        })

        mViewModel.noInternetException.observe(this, androidx.lifecycle.Observer {

        })

        mViewModel.myContactListSuccessResponse.observe(this, androidx.lifecycle.Observer {
            contactModelList.clear()
            val result: String = it.string()
            val myContactRootModel = Gson().fromJson(result, MyContactRootModel::class.java)
            contactModelList.addAll(myContactRootModel.contacts!!)
            myContactAdapter?.apply {
                notifyDataSetChanged()
            }
            linearLayoutProgressBar.beGone()
        })
        mViewModel.myContactListErrorResponse.observe(this, androidx.lifecycle.Observer {
            linearLayoutProgressBar.beGone()
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewModel.onDetach()
    }

}