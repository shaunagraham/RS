package com.rap.sheet.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope

import com.google.gson.JsonObject
import com.rap.sheet.application.BaseApplication
import com.rap.sheet.retrofit.RestInterface
import com.rap.sheet.utilitys.Constant.SUCCESS_INSERTED
import com.rap.sheet.utilitys.Constant.SUCCESS_STATUS
import com.rap.sheet.utilitys.Constant.UNPROCESSABLE_ENTITY
import kotlinx.android.synthetic.main.activity_comment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.RequestBody
import okhttp3.ResponseBody

class CommentViewModel constructor(private val restInterface: RestInterface) : BaseViewModel() {

    val getCommentSuccessResponse = MutableLiveData<ResponseBody>()
    val getCommentErrorResponse = MutableLiveData<ResponseBody>()
    val addCommentSuccessResponse = MutableLiveData<ResponseBody>()
    val addCommentErrorResponse = MutableLiveData<ResponseBody>()
    val deleteCommentSuccessResponse = MutableLiveData<ResponseBody>()
    val deleteCommentErrorResponse = MutableLiveData<ResponseBody>()
    val editCommentSuccessResponse = MutableLiveData<ResponseBody>()
    val editCommentErrorResponse = MutableLiveData<ResponseBody>()
    val makeJsonForAddNewComment = MutableLiveData<JsonObject>()
    val TAG = CommentViewModel::class.java.simpleName

    fun getAllComments(id: String) {
        viewModelScope.launch(apiException("all") + Dispatchers.Main) {
            val response = restInterface.getAllComment(id)

            when (response.code()) {
                SUCCESS_STATUS, SUCCESS_INSERTED -> {
                    getCommentSuccessResponse.postValue(response.body())
                }
                UNPROCESSABLE_ENTITY -> {
                    unAuthorizationException.postValue(true)
                }
                else -> {
                    getCommentErrorResponse.postValue(response.errorBody())
                }
            }

        }
    }

//    fun addComment(data:String){
//        viewModelScope.launch(apiException("add") + Dispatchers.Main) {
//            val response = restInterface.addNewComment(data)
//            Log.i(TAG, "addComment:11 "+response.code())
//            when (response.code()) {
//                SUCCESS_STATUS, SUCCESS_INSERTED -> {
//                    Log.i(TAG, "addComment:sucess "+response.message())
//                    addCommentSuccessResponse.postValue(response.body())
//                }
//                UNPROCESSABLE_ENTITY -> {
//                    Log.i(TAG, "addComment:422 ")
//                    unAuthorizationException.postValue(true)
//                }
//                else -> {
//                    Log.i(TAG, "addComment: "+response.errorBody())
//                    addCommentErrorResponse.postValue(response.errorBody())
//                }
//            }
//
//        }
//
//    }

    fun addComment(conatctId: RequestBody, msg: RequestBody, rate: RequestBody, useId: RequestBody) {
        viewModelScope.launch(apiException("add") + Dispatchers.Main) {
            val response = restInterface.addNewComment(conatctId, msg, rate, useId)
            Log.i(TAG, "addComment:11 " + response.code())
            when (response.code()) {
                SUCCESS_STATUS, SUCCESS_INSERTED -> {
                    Log.i(TAG, "addComment:sucess " + response.message())
                    addCommentSuccessResponse.postValue(response.body())
                }
                UNPROCESSABLE_ENTITY -> {
                    Log.i(TAG, "addComment:422 ")
                    unAuthorizationException.postValue(true)
                }
                else -> {
                    Log.i(TAG, "addComment: " + response.errorBody())
                    addCommentErrorResponse.postValue(response.errorBody())
                }
            }

        }
    }

    fun editComment(message: String, comment_id: String, rate: String) {
        viewModelScope.launch(apiException("edit") + Dispatchers.Main) {
            val response = restInterface.editComment(message, comment_id, rate)
            Log.i(TAG, "editComment:11 " + response.code())
            when (response.code()) {
                SUCCESS_STATUS, SUCCESS_INSERTED -> {
                    Log.i(TAG, "editComment:sucess " + response.message())
                    editCommentSuccessResponse.postValue(response.body())
                }
                UNPROCESSABLE_ENTITY -> {
                    Log.i(TAG, "editComment:422 ")
                    unAuthorizationException.postValue(true)
                }
                else -> {
                    Log.i(TAG, "editComment: " + response.errorBody())
                    editCommentErrorResponse.postValue(response.errorBody())
                }
            }

        }
    }

    fun deleteComment(id: String) {
        viewModelScope.launch(apiException("delete") + Dispatchers.Main) {
            val response = restInterface.deleteComment(id)
            when (response.code()) {
                SUCCESS_STATUS, SUCCESS_INSERTED -> {
                    deleteCommentSuccessResponse.postValue(response.body())
                }
                UNPROCESSABLE_ENTITY -> {
                    unAuthorizationException.postValue(true)
                }
                else -> {
                    deleteCommentErrorResponse.postValue(response.errorBody())
                }
            }
        }
    }

    fun makeJsonForAddNewComment(id: String, msg: String, useId: Int) {
        val jsonObjectRoot = JsonObject()
        jsonObjectRoot.addProperty("contact_id", id)
        jsonObjectRoot.addProperty("message", msg)
        jsonObjectRoot.addProperty("rate", "0.0")
        jsonObjectRoot.addProperty("user_id", useId)
        makeJsonForAddNewComment.postValue(jsonObjectRoot)
    }

    fun onDetach() {
        viewModelScope.cancel()
    }

}
