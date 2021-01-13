package com.rap.sheet.view.feedback

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.rap.sheet.R
import com.rap.sheet.databinding.ActivityFeedbackBinding
import com.rap.sheet.extenstion.click
import com.rap.sheet.extenstion.makeSnackBar
import com.rap.sheet.model.Feedbacks.FeedbacksModel
import com.rap.sheet.utilitys.InternetConnection
import com.rap.sheet.utilitys.Utility
import com.rap.sheet.view.common.BaseActivity
import com.rap.sheet.viewmodel.FeedbackViewModel
import kotlinx.android.synthetic.main.activity_comment.*
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.logger.MESSAGE

class FeedbackActivity : BaseActivity() {
    private lateinit var binding: ActivityFeedbackBinding
    private var message: String? = null
    private var firstName: String? = null
    private var lastName: String? = null
    private var email: String? = null
    private var phone: String? = null
    private val mViewModel: FeedbackViewModel by viewModel()
    private val TAG = FeedbackActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_feedback)
        setView()
        listenToViewModel()
    }

    private fun listenToViewModel() {

        mViewModel.sendFeedbackSuccessResponse.observe(this, Observer {
            dismissProgressDialog()
            val result: String = it.string()
            val jsonObjectResult = JSONObject(result)
            val feedbacksModel = Gson().fromJson(jsonObjectResult.toString(), FeedbacksModel::class.java)
            Toast.makeText(this@FeedbackActivity, "" + feedbacksModel.data.message, Toast.LENGTH_SHORT)
            finish()
        })

        mViewModel.sendFeedbackErrorResponse.observe(this, Observer {
            dismissProgressDialog()
            Log.i(TAG, "listenToViewModel: ")
        })

        mViewModel.unAuthorizationException.observe(this, Observer {
            dismissProgressDialog()
        })

    }

    private fun setView() {

        setUpToolBar()

        binding.buttonCancel.click {
            onBackPressed()
        }

        binding.buttonAdd.click {
            validate()
        }

    }

    private fun validate() {
        message = binding.editTextMessage.text.toString().trim()
        firstName = binding.editTextFirstName.text.toString().trim()
        lastName = binding.editTextLastName.text.toString().trim()
        email = binding.editTextEmail.text.toString().trim()
        phone = binding.editTextPhoneNumber.text.toString().trim()
        if (!Utility.validateEditText(firstName!!)) {
            makeSnackBar(binding.editTextFirstName, resources.getString(R.string.empty_first_name))
        } else if (!Utility.validateEditText(lastName!!)) {
            makeSnackBar(binding.editTextLastName, resources.getString(R.string.empty_last_name))
        } else if (!Utility.validateEditText(phone!!)) {
            makeSnackBar(binding.editTextPhoneNumber, resources.getString(R.string.empty_phone))
        } else if (!Utility.isValidMobile(phone)) {
            makeSnackBar(binding.editTextPhoneNumber, resources.getString(R.string.valid_phone))
        } else if (!Utility.validateEditText(email!!)) {
            makeSnackBar(binding.editTextEmail, resources.getString(R.string.empty_email))
        } else if (!Utility.isValidEmail(email)) {
            makeSnackBar(binding.editTextEmail, resources.getString(R.string.invalid_email))
        } else if (!Utility.validateEditText(message!!)) {
            makeSnackBar(binding.editTextMessage, resources.getString(R.string.empty_message))
        } else if (InternetConnection.checkConnection(this@FeedbackActivity)) {
            callSendFeedback()
        } else {
            Toast.makeText(this, resources.getString(R.string.no_internet), Toast.LENGTH_SHORT)
        }
    }

    private fun callSendFeedback() {
        launchProgressDialog()

        mViewModel.makeJsonForAddFeedback(message!!, firstName!!, lastName!!, email!!, phone!!)
        mViewModel.makeJsonForAddFeedback.observe(this, Observer {
            mViewModel.sendFeedback(it)
        })
//        mViewModel.sendFeedback(message,firstName,lastName,email,phone)
    }

    private fun setUpToolBar() {
        binding.toolbar.imgClose.click {
            onBackPressed()
        }
        binding.toolbar.tvTitle.text = resources.getString(R.string.feed_back)
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewModel.onDetach()
    }


}