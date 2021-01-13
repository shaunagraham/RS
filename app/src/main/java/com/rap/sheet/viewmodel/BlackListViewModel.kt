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

class BlackListViewModel constructor(private val restInterface: RestInterface) : BaseViewModel() {

    val blackListContactSuccessResponse= MutableLiveData<ResponseBody>()
    val blackListContactErrorResponse= MutableLiveData<ResponseBody>()

    fun getBlackListContacts(){
        viewModelScope.launch(apiException() + Dispatchers.Main) {
            val response = restInterface.browseContact()

            when (response.code()) {
                SUCCESS_STATUS,SUCCESS_INSERTED -> {
                    blackListContactSuccessResponse.postValue(response.body())
                }
                UNPROCESSABLE_ENTITY->{
                    unAuthorizationException.postValue(true)
                }
                else -> {
                    blackListContactErrorResponse.postValue(response.errorBody())
                }
            }

        }
    }

    fun onDetach() {
        viewModelScope.cancel()
    }

}
