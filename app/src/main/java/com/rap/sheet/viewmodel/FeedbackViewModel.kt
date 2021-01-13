package com.rap.sheet.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.rap.sheet.retrofit.RestInterface
import com.rap.sheet.utilitys.Constant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.RequestBody
import okhttp3.ResponseBody

class FeedbackViewModel constructor(private val restInterface: RestInterface) : BaseViewModel() {

    val sendFeedbackSuccessResponse = MutableLiveData<ResponseBody>()
    val sendFeedbackErrorResponse = MutableLiveData<ResponseBody>()
    val makeJsonForAddFeedback = MutableLiveData<JsonObject>()

    val TAG = FeedbackViewModel::class.java.simpleName

    fun sendFeedback(jsonObject: JsonObject) {
        viewModelScope.launch(apiException("feedback") + Dispatchers.Main) {
            val response = restInterface.sendFeedback(jsonObject)
            when (response.code()) {
                Constant.SUCCESS_STATUS, Constant.SUCCESS_INSERTED -> {
                    Log.i(TAG, "sendFeedback:success ")
                    sendFeedbackSuccessResponse.postValue(response.body())
                }
                Constant.UNPROCESSABLE_ENTITY -> {
                    Log.i(TAG, "sendFeedback:enity ")
                    unAuthorizationException.postValue(true)
                }
                else -> {
                    Log.i(TAG, "sendFeedback:error ")
                    sendFeedbackErrorResponse.postValue(response.errorBody())
                }
            }
        }
    }

    fun makeJsonForAddFeedback(message: String, first_name: String, last_name: String, email: String, phone: String) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("message", message)
        jsonObject.addProperty("first_name", first_name)
        jsonObject.addProperty("last_name", last_name)
        jsonObject.addProperty("email", email)
        jsonObject.addProperty("phone", phone)
        makeJsonForAddFeedback.postValue(jsonObject)
    }

    fun onDetach() {
        viewModelScope.cancel()
    }

}