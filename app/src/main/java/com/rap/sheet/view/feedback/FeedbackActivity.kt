package com.rap.sheet.view.feedback

import android.os.Bundle
import com.google.gson.Gson
import com.rap.sheet.R
import com.rap.sheet.extenstion.click
import com.rap.sheet.extenstion.makeSnackBar
import com.rap.sheet.extenstion.showToast
import com.rap.sheet.model.Feedbacks.FeedbacksModel
import com.rap.sheet.utilitys.InternetConnection
import com.rap.sheet.utilitys.Utility
import com.rap.sheet.view.common.BaseActivity
import com.rap.sheet.viewmodel.FeedbackViewModel
import kotlinx.android.synthetic.main.activity_comment.*
import kotlinx.android.synthetic.main.activity_comment.buttonAdd
import kotlinx.android.synthetic.main.activity_comment.buttonCancel
import kotlinx.android.synthetic.main.activity_feedback.*
import kotlinx.android.synthetic.main.top_view_layout.view.*
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel

class FeedbackActivity : BaseActivity() {
//    private lateinit var binding: ActivityFeedbackBinding
    private var message: String? = null
    private var firstName: String? = null
    private var lastName: String? = null
    private var email: String? = null
    private var phone: String? = null
    private val mViewModel: FeedbackViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)
        setView()
        listenToViewModel()
    }

    private fun listenToViewModel() {

        mViewModel.sendFeedbackSuccessResponse.observe(this, {
            dismissProgressDialog()
            val result: String = it.string()
            val jsonObjectResult = JSONObject(result)
            val feedbacksModel = Gson().fromJson(jsonObjectResult.toString(), FeedbacksModel::class.java)
            feedbacksModel.data.message.showToast(this@FeedbackActivity)
//            Toast.makeText(this@FeedbackActivity, "" + feedbacksModel.data.message, Toast.LENGTH_SHORT)
            finish()
        })

        mViewModel.sendFeedbackErrorResponse.observe(this, {
            dismissProgressDialog()
        })

        mViewModel.unAuthorizationException.observe(this, {
            dismissProgressDialog()
        })

    }

    private fun setView() {

        setUpToolBar()

        buttonCancel.click {
            onBackPressed()
        }

        buttonAdd.click {
            validate()
        }

    }

    private fun validate() {
        message = editTextMessage.text.toString().trim()
        firstName = editTextFirstName.text.toString().trim()
        lastName = editTextLastName.text.toString().trim()
        email = editTextEmail.text.toString().trim()
        phone = editTextPhoneNumber.text.toString().trim()
        if (!Utility.validateEditText(firstName!!)) {
            makeSnackBar(editTextFirstName, resources.getString(R.string.empty_first_name))
        } else if (!Utility.validateEditText(lastName!!)) {
            makeSnackBar(editTextLastName, resources.getString(R.string.empty_last_name))
        } else if (!Utility.validateEditText(phone!!)) {
            makeSnackBar(editTextPhoneNumber, resources.getString(R.string.empty_phone))
        } else if (!Utility.isValidMobile(phone)) {
            makeSnackBar(editTextPhoneNumber, resources.getString(R.string.valid_phone))
        } else if (!Utility.validateEditText(email!!)) {
            makeSnackBar(editTextEmail, resources.getString(R.string.empty_email))
        } else if (!Utility.isValidEmail(email)) {
            makeSnackBar(editTextEmail, resources.getString(R.string.invalid_email))
        } else if (!Utility.validateEditText(message!!)) {
            makeSnackBar(editTextMessage, resources.getString(R.string.empty_message))
        } else if (InternetConnection.checkConnection(this@FeedbackActivity)) {
            callSendFeedback()
        } else {
            resources.getString(R.string.no_internet).showToast(this)
        }
    }

    private fun callSendFeedback() {
        launchProgressDialog()

        mViewModel.makeJsonForAddFeedback(message!!, firstName!!, lastName!!, email!!, phone!!)
        mViewModel.makeJsonForAddFeedback.observe(this, {
            mViewModel.sendFeedback(it)
        })
//        mViewModel.sendFeedback(message,firstName,lastName,email,phone)
    }

    private fun setUpToolBar() {
        toolbar.imgClose.click {
            onBackPressed()
        }
        toolbar.tvTitle.text = resources.getString(R.string.feed_back)
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewModel.onDetach()
    }


}