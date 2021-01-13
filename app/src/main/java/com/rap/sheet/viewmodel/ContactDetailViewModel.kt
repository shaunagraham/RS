package com.rap.sheet.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope

import com.google.gson.JsonObject
import com.rap.sheet.retrofit.RestInterface
import com.rap.sheet.utilitys.Constant.SUCCESS_INSERTED
import com.rap.sheet.utilitys.Constant.SUCCESS_STATUS
import com.rap.sheet.utilitys.Constant.UNPROCESSABLE_ENTITY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.ResponseBody

class ContactDetailViewModel constructor(private val restInterface: RestInterface) : BaseViewModel() {

    val contactDetailSuccessResponse= MutableLiveData<ResponseBody>()
    val contactDetailErrorResponse= MutableLiveData<ResponseBody>()
    val deleteCommentSuccessResponse= MutableLiveData<ResponseBody>()
    val deleteCommentErrorResponse= MutableLiveData<ResponseBody>()

    fun getContactDetail(id:String){
        viewModelScope.launch(apiException("detail") + Dispatchers.Main) {
            val response = restInterface.contactDetails(id)

            when (response.code()) {
                SUCCESS_STATUS,SUCCESS_INSERTED -> {
                    contactDetailSuccessResponse.postValue(response.body())
                }
                UNPROCESSABLE_ENTITY->{
                    unAuthorizationException.postValue(true)
                }
                else -> {
                    contactDetailErrorResponse.postValue(response.errorBody())
                }
            }

        }
    }

    fun deleteComment(id:String){
        viewModelScope.launch(apiException("delete") + Dispatchers.Main) {
            val response=restInterface.deleteComment(id)
            when (response.code()) {
                SUCCESS_STATUS,SUCCESS_INSERTED -> {
                    deleteCommentSuccessResponse.postValue(response.body())
                }
                UNPROCESSABLE_ENTITY->{
                    unAuthorizationException.postValue(true)
                }
                else -> {
                    deleteCommentErrorResponse.postValue(response.errorBody())
                }
            }
        }
    }

    fun onDetach() {
        viewModelScope.cancel()
    }

}
