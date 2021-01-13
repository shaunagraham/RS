package com.rap.sheet.view.contact_detail

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.rap.sheet.BuildConfig
import com.rap.sheet.R
import com.rap.sheet.adapter.ContactCommentAdapter
import com.rap.sheet.extenstion.*
import com.rap.sheet.model.ContactDetail.ContactDetailCommentModel
import com.rap.sheet.model.ContactDetail.ContactDetailRootModel
import com.rap.sheet.utilitys.*
import com.rap.sheet.view.comment.CommentActivity
import com.rap.sheet.view.common.BaseActivity
import com.rap.sheet.viewmodel.ContactDetailViewModel
import kotlinx.android.synthetic.main.activity_contact_detail.*
import kotlinx.android.synthetic.main.activity_contact_detail.textViewMessage
import kotlinx.android.synthetic.main.no_internet_view.*
import kotlinx.android.synthetic.main.progress_dialog_view.*
import kotlinx.android.synthetic.main.toolbar_new.*

import org.koin.androidx.viewmodel.ext.android.viewModel
import java.nio.file.Files.delete
import java.util.*

class ContactDetailActivity : BaseActivity() {
    private var contactDetailCommentModelList: MutableList<ContactDetailCommentModel> = mutableListOf()
    private val mViewModel: ContactDetailViewModel by viewModel()
    private var contactCommentAdapter: ContactCommentAdapter? = null

    // TODO: Rename and change types of parameters
    private var contactDetailRootModel: ContactDetailRootModel? = null
    private var id: String? = null

    private var number: String? = null
    private var deletePos: Int = 0
    private var itemTouchHelper: ItemTouchHelper? = null
    val TAG = ContactDetailActivity::class.java.simpleName

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_detail)

        // searchViewModel = ViewModelProviders.of(this).get(SearchViewModel.class);
        // restInterface = RetrofitClient.getClient(BuildConfig.APIPATH).create(RestInterface::class.java)

        getIntentData()
        initView()
        setUpToolBar()
        viewClickListener()
        listenToViewModel()

    }

    private fun listenToViewModel() {

        mViewModel.contactDetailSuccessResponse.observe(this, androidx.lifecycle.Observer {
            val result: String = it.string()
            contactDetailRootModel = ContactDetailRootModel()
            contactDetailRootModel = Gson().fromJson(result, ContactDetailRootModel::class.java)
            setUpContactDetailsData()
        })

        mViewModel.unAuthorizationException.observe(this, androidx.lifecycle.Observer {
            displayAlertDialog(desc = resources.getString(R.string.contact_delete_msg), positiveText = resources.getString(android.R.string.ok), positiveClick = object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    dialog?.apply {
                        dismiss()
                        finish()
                    }
                }

            })
            progressBar.beGone()
        })

        mViewModel.contactDetailErrorResponse.observe(this, androidx.lifecycle.Observer {
            progressBar.beGone()
            reLoadAPI()
        })

        mViewModel.deleteCommentSuccessResponse.observe(this, androidx.lifecycle.Observer {
            dismissProgressDialog()
            contactDetailCommentModelList.removeAt(deletePos)
            contactCommentAdapter?.apply {
                notifyItemRemoved(deletePos)
            }
            if (contactDetailCommentModelList.isEmpty()) {
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(Intent(Constant.REFRESH_CONTACTS).putExtra("refresh_contact", true))
            }
            checkCommentExists()
        })

        mViewModel.deleteCommentErrorResponse.observe(this, androidx.lifecycle.Observer {
            dismissProgressDialog()
        })

        mViewModel.noInternetException.observe(this, androidx.lifecycle.Observer {
            if (it == "detail") {
                progressBar.beGone()
                if (InternetConnection.checkConnection(this@ContactDetailActivity)) {
                    reLoadAPI()
                } else {
                    constraintLayoutNoInternet.beVisible()
                    relativeLayoutProfileRoot.beGone()
                }
            } else {
                if (InternetConnection.checkConnection(this)) {
                    this.displayAlertDialog(desc = resources.getString(R.string.something_wrong), cancelable = false, positiveText = resources.getString(android.R.string.ok), positiveClick = object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, which: Int) {
                            dialog?.apply {
                                this.dismiss()
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
            }
        })

    }

    private fun launchHistorySearchData() {
        itemTouchHelper = ItemTouchHelper(object : SwipeHelperDelete(this@ContactDetailActivity, recyclerViewComment, contactDetailCommentModelList, sharedPreferences.userID) {
            override fun instantiateUnderlayButton(viewHolder: RecyclerView.ViewHolder?,
                                                   underlayButtons: MutableList<UnderlayButton>?) {
                Log.i(TAG, "instantiateUnderlayButton: " + underlayButtons!!.size)
                underlayButtons.add(UnderlayButton(
                        "",
                        R.drawable.delete3,
                        Color.parseColor("#000000"),
                        false,
                        object : UnderlayButtonClickListener {
                            override fun onClick(pos: Int) {
                                deletePos = pos
                                displayAlertDialog(title = resources.getString(R.string.delete), desc = resources.getString(R.string.delete_comment_msg), positiveText = resources.getString(R.string.yes), positiveClick = DialogInterface.OnClickListener { dialog, _ ->
                                    dialog?.apply {
                                        dismiss()
                                        launchProgressDialog()
                                        mViewModel.deleteComment(contactDetailCommentModelList[pos].id.toString())
                                    }
                                }, negativeText = resources.getString(R.string.cancel), negativeClick = DialogInterface.OnClickListener { dialog, which ->
                                    dialog?.apply {
                                        dismiss()
                                    }
                                })
                            }
                        }
                ))
            }
        })
        itemTouchHelper?.attachToRecyclerView(recyclerViewComment)
    }

    private fun setUpToolBar() {
//        toolbar.title = ""
//        setSupportActionBar(toolbar)
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        toolbar.setNavigationOnClickListener {
//            onBackPressed()
//        }
        imageViewBack.click { onBackPressed() }
    }

    private fun getIntentData() {
        intent.extras?.apply {
            id = this.getString("id")
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun viewClickListener() {
//        floatingButtonComment.click {
//            val bundle = Bundle()
//            bundle.putString("id", contactDetailRootModel?.details?.id)
//            startActivityFromActivityWithBundleCode<CommentActivity>(bundle, Constant.UPDATE_COMMENT)
//        }
        floatingButtonShare.click {
            contactDetailRootModel?.apply {
                this.details?.apply {
                    val sendIntent = Intent()
                    sendIntent.action = Intent.ACTION_SEND
                    val email: String = if ((this.email != null && this.email != "")) "\nEmail: " + this.email else ""
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=" + packageName + "\nContact name: " + this.firstName + " " + this.lastName + "\n" + "Contact Number: " + this.number.toString().replace("-", "") + email)
                    sendIntent.type = "text/plain"
                    startActivity(Intent.createChooser(sendIntent, "Share Contact"))
                }
            }
        }

        floatingButtonMessage.click {
            contactDetailRootModel?.details?.apply {
                Utility.callUserSMS(this@ContactDetailActivity, this.number)
            }
        }

        floatingButtonCall.click {
            contactDetailRootModel?.details?.apply {

                val permissionCheck: Int = ContextCompat.checkSelfPermission(this@ContactDetailActivity, Manifest.permission.CALL_PHONE)
                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(Manifest.permission.CALL_PHONE), 103)
                } else {
                    Utility.callUserCall(this@ContactDetailActivity, this.number)
                }
            }
        }

        floatingButtonSave.click {
            contactDetailRootModel?.details?.apply {
                Utility.saveNewNumber(this@ContactDetailActivity, this.number, this.firstName + " " + this.lastName)
            }

        }

        fabAddNewComment.click {
            val bundle = Bundle()
            bundle.putString("id", contactDetailRootModel?.details?.id)
            startActivityFromActivityWithBundleCode<CommentActivity>(bundle, Constant.UPDATE_COMMENT)
        }

        imageViewWebsite.click {
            contactDetailRootModel?.details?.apply {
                var webLink: Uri? = Uri.parse(this.weblink)
                if (!this.weblink.toString().startsWith("http://") &&
                        !this.weblink.toString().startsWith("https://")) {
                    webLink = Uri.parse("http://" + this.weblink.toString())
                }
                startActivity(Intent(Intent.ACTION_VIEW, webLink))
            }
        }

        imageViewInsta.click {
            contactDetailRootModel?.details?.apply {
                val uri: Uri
                val instagramUser: String = this.instagram.toString().replace("@", "")
                uri = if (!instagramUser.startsWith("http://") && !instagramUser.startsWith("https://")) {
                    Uri.parse("http://" + this.instagram.toString())
                } else {
                    Uri.parse("http://instagram.com/_u/$instagramUser")
                }
                val likeIng: Intent = Intent(Intent.ACTION_VIEW, uri)
                likeIng.setPackage("com.instagram.android")
                try {
                    startActivity(likeIng)
                } catch (e: ActivityNotFoundException) {
                    startActivity(Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://instagram.com/$instagramUser")))
                }
            }
        }

        imageViewFacebook.click {
            contactDetailRootModel?.details?.apply {
                var facebookPage: Uri? = Uri.parse(this.facebook)
                if (!this.facebook.toString().startsWith("http://") &&
                        !this.facebook.toString().startsWith("https://")) {
                    facebookPage = Uri.parse("http://" + this.facebook.toString())
                }
                startActivity(Intent(Intent.ACTION_VIEW, facebookPage))
            }
        }

        imageViewLinkedIn.click {
            contactDetailRootModel?.details?.apply {
                var linkedInPage: Uri? = Uri.parse(this.linkedin.toString())
                if (!this.linkedin.toString().startsWith("http://") && !this.linkedin.toString().startsWith("https://")) {
                    linkedInPage = Uri.parse("http://" + this.linkedin.toString())
                }
                startActivity(Intent(Intent.ACTION_VIEW, linkedInPage))
            }
        }

        // imageViewMoreInfo.setOnClickListener(imageViewMoreInfoClickListener);
        buttonTryAgain.click {
            progressBar.beVisible()
            constraintLayoutNoInternet.beGone()
            launchContactDetailAPI()
        }
    }

    private fun initView() {
        recyclerViewComment.layoutManager = LinearLayoutManager(this)

//        Log.i(TAG, "initView: "+contactDetailCommentModelList.size)
//        for (i in contactDetailCommentModelList.indices){
//            if (contactDetailCommentModelList[i].userId == sharedPreferences.userID) {
//                Log.i(TAG, "initView: "+contactDetailCommentModelList.get(i).userId)
//            }
//        }
    }

    private fun launchContactDetailAPI() {
        mViewModel.getContactDetail(id.toString())
//        contactDetails = restInterface!!.contactDetails(id)
//        contactDetails!!.enqueue(contactDetailsCallBack)
    }

    private fun setUpContactDetailsData() {
//        linearLayoutOtherOption.beVisible()
//        floatingButtonComment!!.show()
        progressBar.beGone()
        relativeLayoutProfileRoot.beVisible()
        contactDetailRootModel?.apply {
            this.details?.apply {
                textViewContactName.text = Utility.capitalize(this.firstName + " " + this.lastName)
                val stringBuilderPhone: StringBuilder = StringBuilder(this.number.toString().replace("-".toRegex(), ""))
                if (stringBuilderPhone.length > 3) {
                    stringBuilderPhone.insert(3, "-")
                }
                if (stringBuilderPhone.length > 7) {
                    stringBuilderPhone.insert(7, "-")
                }

                if (this.email != null && this.email != "") {
                    textViewContactEmail.text = this.email
                    textViewContactEmail.beVisible()
                } else {
                    textViewContactEmail.beInVisible()
                }

                if (this.profile != null && this.profile != "") {
                    imageViewProfile.beVisible()
                    Glide.with(this@ContactDetailActivity).load(BuildConfig.IMAGE_PATH + this.profile).into((imageViewProfile)!!)
                } else {
                    imageViewProfile.beGone()
                }
//                if (this.weblink != null && this.weblink != "") {
//                    imageViewWebsite.beVisible()
//                } else {
//                    imageViewWebsite.beGone()
//                }
//                if (this.instagram != null && this.instagram != "") {
//                    imageViewInsta.beVisible()
//                } else {
//                    imageViewInsta.beGone()
//                }
//                if (this.facebook != null && this.facebook.toString().contains("www.facebook.com")) {
//                    imageViewFacebook.beVisible()
//                } else {
//                    imageViewFacebook.beGone()
//                }
//                if (this.linkedin != null && this.linkedin != "") {
//                    imageViewLinkedIn.beVisible()
//                } else {
//                    imageViewLinkedIn.beGone()
//                }
                textViewContactNumber.text = stringBuilderPhone.toString()
            }
            contactDetailCommentModelList.clear()
            this.comments?.apply {
                contactDetailCommentModelList.addAll(this)
                contactCommentAdapter = ContactCommentAdapter(contactDetailCommentModelList, false, sharedPreferences.userID, onDeleteClick = ::onCommentDeleteClick)
                recyclerViewComment.adapter = contactCommentAdapter
            }
        }
        checkCommentExists()
    }

    private fun onCommentDeleteClick(position: Int) {
        val bundle = Bundle()
        bundle.putString("id", contactDetailRootModel?.details?.id)
        bundle.putString("comment_id", contactDetailCommentModelList[position].id.toString())
        bundle.putString("message", contactDetailCommentModelList[position].message)
        bundle.putBoolean("showEdit", true)
        startActivityFromActivityWithBundleCode<CommentActivity>(bundle, Constant.UPDATE_COMMENT)
//        Toast.makeText(this, "" + position, Toast.LENGTH_SHORT).show()
//        deletePos = position
//        displayAlertDialog(title = resources.getString(R.string.delete), desc = resources.getString(R.string.delete_comment_msg)
//                , positiveText = resources.getString(R.string.yes), positiveClick = DialogInterface.OnClickListener { dialog, _ ->
//            dialog?.apply {
//                dismiss()
//                launchProgressDialog()
//                mViewModel.deleteComment(contactDetailCommentModelList[position].id.toString())
//            }
//        }, negativeText = resources.getString(R.string.cancel), negativeClick = DialogInterface.OnClickListener { dialog, which ->
//            dialog?.apply {
//                dismiss()
//            }
//        })
    }

    private fun reLoadAPI() {
        displayAlertDialog(desc = resources.getString(R.string.something_wrong), positiveText = resources.getString(R.string.retry), positiveClick = DialogInterface.OnClickListener { dialog, which ->
            dialog?.apply {
                dismiss()
                progressBar.beVisible()
                launchContactDetailAPI()
            }
        }, negativeText = resources.getString(R.string.cancel), negativeClick = DialogInterface.OnClickListener { dialog, which ->
            dialog?.apply {
                dismiss()
            }
        })
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if ((grantResults.isNotEmpty()) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            Utility.callUserCall(this, number)
        } else {
            Log.d("TAG", "Call Permission Not Granted")
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constant.UPDATE_COMMENT) {
            if (data != null) {
//                if (data.getStringExtra("id") != null) {
                var contactDetailCommentModelListTemp: List<ContactDetailCommentModel>? = ArrayList()
                contactDetailCommentModelListTemp = data.getSerializableExtra("comment") as List<ContactDetailCommentModel>?
                contactDetailCommentModelList.addAll(0, (contactDetailCommentModelListTemp)!!)
                contactCommentAdapter?.apply {
                    notifyDataSetChanged()
                }
                if (recyclerViewComment.visibility == View.GONE) {
                    recyclerViewComment.beVisible()
                }
                checkCommentExists()
            }
        }
    }

    private fun checkCommentExists() {
        if (contactDetailCommentModelList.size > 0) {
            //linearLayoutNoMessage.setVisibility(View.GONE);
            textViewMessage.beVisible()
            textViewMessage.text = resources.getString(R.string.add_comment)
            fabAddNewComment.beVisible()
            recyclerViewComment.beVisible()
        } else {
            textViewMessage.beVisible()
            textViewMessage.text = resources.getString(R.string.no_comment_msg)
            fabAddNewComment.beVisible()
            recyclerViewComment.beGone()
        }
        launchHistorySearchData()

//        for (i in contactDetailCommentModelList.indices){
//            if (contactDetailCommentModelList[i].userId == sharedPreferences.userID) {
//                Log.i(TAG, "initView: "+contactDetailCommentModelList.get(i).userId)
//            }
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewModel.onDetach()
    }

    override fun onResume() {
        super.onResume()
        launchContactDetailAPI()

    }

}