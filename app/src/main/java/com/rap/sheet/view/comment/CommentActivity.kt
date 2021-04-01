package com.rap.sheet.view.comment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.gson.Gson
import com.rap.sheet.R
import com.rap.sheet.adapter.ContactCommentAdapter
import com.rap.sheet.extenstion.click
import com.rap.sheet.extenstion.displayAlertDialog
import com.rap.sheet.extenstion.makeSnackBar
import com.rap.sheet.extenstion.userID
import com.rap.sheet.model.ContactDetail.ContactDetailCommentModel
import com.rap.sheet.model.SearchConatct.CommentRootModel
import com.rap.sheet.utilitys.Constant
import com.rap.sheet.utilitys.InternetConnection
import com.rap.sheet.utilitys.Utility
import com.rap.sheet.view.common.BaseActivity
import com.rap.sheet.viewmodel.CommentViewModel
import kotlinx.android.synthetic.main.activity_comment.*
import kotlinx.android.synthetic.main.toolbar.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.IOException
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList


class CommentActivity : BaseActivity() {
    private val mViewModel: CommentViewModel by viewModel()
    private var stringContactId: String? = null
    private var contactDetailCommentModelList: MutableList<ContactDetailCommentModel> = mutableListOf()
    private var contactDetailCommentModelListTemp: MutableList<ContactDetailCommentModel> = mutableListOf()
    private var contactCommentAdapter: ContactCommentAdapter? = null
    private var isStatus: Boolean = false
    private var pos: Int = 0
    private var commentID: String? = null
    private var message: String? = null
    private var showEdit: Boolean = false
    private var commentArrayList: MutableList<String> = mutableListOf()
    private var commentSelectedArrayList: ArrayList<String> = ArrayList()
    private var comment: String =""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)
        getIntentData()
        setUpToolBar()
        initView()
        listenToViewModel()
        viewClickListener()
        commentArrayList.add("Telemarketer")
        commentArrayList.add("Robocaller")
        commentArrayList.add("Fraud")
        commentArrayList.add("Spoof")
        commentArrayList.add("Spam")
        commentArrayList.add("Survey")
        commentArrayList.add("IRS")
        commentArrayList.add("Debt Collector")
        commentArrayList.add("Charity")
        commentArrayList.add("Political")

        setCategoryChips(commentArrayList)

    }

    private fun listenToViewModel() {

//        mViewModel.makeJsonForAddNewComment.observe(this, androidx.lifecycle.Observer {
//            mViewModel.addComment(it.toString())
//        })

        mViewModel.addCommentSuccessResponse.observe(this, {
            dismissProgressDialog()
            editTextComment!!.setText("")
            //   ratingBarComment.setRating(0);
            val result: String = it.string()
            val jsonObjectResult = JSONObject(result)
            val contactDetailCommentModel = Gson().fromJson(jsonObjectResult.getString("data"), ContactDetailCommentModel::class.java)
            contactDetailCommentModelList.add(0, contactDetailCommentModel)
            contactDetailCommentModelListTemp.add(contactDetailCommentModel)
            contactCommentAdapter?.apply {
                notifyDataSetChanged()
            }
            finish()
//            onBackPressed()
        })

        mViewModel.unAuthorizationException.observe(this, {
            dismissProgressDialog()
        })

        mViewModel.addCommentErrorResponse.observe(this, {
            dismissProgressDialog()
        })

        mViewModel.noInternetException.observe(this, {
            dismissProgressDialog()
        })

        mViewModel.deleteCommentSuccessResponse.observe(this, {
            dismissProgressDialog()
            contactDetailCommentModelList.removeAt(pos)
            contactCommentAdapter!!.notifyItemRemoved(pos)
            if (contactDetailCommentModelList.isEmpty()) {
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(Intent(Constant.REFRESH_CONTACTS).putExtra("refresh_contact", true))
            }
        })

        mViewModel.deleteCommentErrorResponse.observe(this, {
            dismissProgressDialog()
        })

        mViewModel.editCommentSuccessResponse.observe(this, {
            dismissProgressDialog()
            contactCommentAdapter?.apply {
                notifyDataSetChanged()
            }
            finish()
        })

        mViewModel.editCommentErrorResponse.observe(this, {
            dismissProgressDialog()
            finish()
        })

        mViewModel.getCommentSuccessResponse.observe(this, {
            dismissProgressDialog()
            try {
                val result: String = it.string()
                val commentRootModel = Gson().fromJson(result, CommentRootModel::class.java)
                contactDetailCommentModelList.addAll(commentRootModel.data!!)
                contactCommentAdapter?.apply {
                    notifyDataSetChanged()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        })

        mViewModel.getCommentErrorResponse.observe(this, {
            dismissProgressDialog()
        })

    }

    private fun getIntentData() {
        intent.extras?.apply {
            stringContactId = this.getString("id")
            message = this.getString("message")
            commentID = this.getString("comment_id")
            isStatus = this.getBoolean("status", false)
            showEdit = this.getBoolean("showEdit", false)
        }
    }

    @SuppressLint("InflateParams")
    fun setCategoryChips(categorys: MutableList<String>) {
        for (category in categorys) {
            val mChip = this.layoutInflater.inflate(R.layout.item_chip_category, null, false) as Chip
            mChip.text = category
            mChip.setPadding(20, 5, 20, 5)
            mChip.setOnCheckedChangeListener { _, b ->
                var position = -1
                if (b) {
                    commentSelectedArrayList.add(category)
                    comment = commentSelectedArrayList.toString().replace("[", "").replace("]", "") //remove brackets([) convert it to string

                } else {
                    for (i in commentSelectedArrayList.indices) {
                        if (commentSelectedArrayList.get(i).contentEquals(category)) {
                            position = i
                        }
                    }
                    commentSelectedArrayList.removeAt(position)
                    comment = commentSelectedArrayList.toString().replace("[", "").replace("]", "") //remove brackets([) convert it to string
                }

            }
            chipGroupTag.addView(mChip)
        }
    }

    private fun initView() {
        recyclerViewComment.layoutManager = LinearLayoutManager(this)
        recyclerViewComment.isNestedScrollingEnabled = false
        contactCommentAdapter = ContactCommentAdapter(contactDetailCommentModelList, true, sharedPreferences.userID, onDeleteClick = ::onCommentDeleteClick)
        recyclerViewComment.adapter = contactCommentAdapter
        launchProgressDialog()
        mViewModel.getAllComments(stringContactId.toString())
    }

    private fun onCommentDeleteClick(position: Int) {
        pos = position
        displayAlertDialog(
                title = resources.getString(R.string.delete),
                desc = resources.getString(R.string.delete_comment_msg),
                positiveText = resources.getString(R.string.yes),
                positiveClick = { dialog, _ ->
                    dialog?.apply {
                        dismiss()
                        launchProgressDialog()
                        mViewModel.deleteComment(contactDetailCommentModelList[position].id.toString())
                    }
                },
                negativeText = this.resources.getString(R.string.cancel),
                negativeClick = { dialog, _ ->
                    dialog?.apply {
                        dismiss()
                    }
                }
        )
    }

    private fun setUpToolBar() {
        toolbar.title = resources.getString(R.string.comment)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        Utility.applyToolBarTitle(this, toolbar)
        if (showEdit) {
            editTextComment.setText(message)
            buttonAdd.text = resources.getString(R.string.edit)
        }
    }

    private fun viewClickListener() {

        buttonCancel.click {
            finish()
        }

        buttonAdd.click {
            if (comment.trim { it <= ' ' }.isEmpty()) {
                makeSnackBar(coordinatorLayoutRoot, resources.getString(R.string.comment_required))
            } else {
                if (InternetConnection.checkConnection(this@CommentActivity)) {
                    if (showEdit) {
                        callEditAPI()
                    } else {
                        callCommentAPI()
                    }
                } else {
                    makeSnackBar(coordinatorLayoutRoot, resources.getString(R.string.no_internet))
                }
            }
        }

    }

    private fun callEditAPI() {
        launchProgressDialog()
        comment.let { commentID?.let { it1 -> mViewModel.editComment(it, it1, "0") } }
    }

    private fun callCommentAPI() {
        launchProgressDialog()

        val user_id: RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), sharedPreferences.userID)
        val contact_id: RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), stringContactId.toString())
        val message: RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), comment.trim { it <= ' ' })
        val rate: RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), "0.0")
//        mViewModel.makeJsonForAddNewComment(stringContactId.toString(), editTextComment.text.toString().trim { it <= ' ' }, sharedPreferences.userID.toInt())
        mViewModel.addComment(contact_id, message, rate, user_id)
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewModel.onDetach()
    }

    override fun onBackPressed() {
        val intent = Intent()
        intent.putExtra("comment", contactDetailCommentModelListTemp as Serializable?)
        setResult(Constant.UPDATE_COMMENT, intent)
        finish()
    }

}