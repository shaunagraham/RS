package com.rap.sheet.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.rap.sheet.retrofit.RestInterface
import com.rap.sheet.utilitys.Constant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.ResponseBody

class GetProfileViewModel constructor(private val restInterface: RestInterface) : BaseViewModel(){
    val getProfileSuccessResponse = MutableLiveData<ResponseBody>()
    val getProfileErrorResponse = MutableLiveData<ResponseBody>()

    fun getUserProfile(query: String) {
        viewModelScope.launch(apiException() + Dispatchers.Main) {
            val response = restInterface.getProfileData(query)

            when (response.code()) {
                Constant.SUCCESS_STATUS, Constant.SUCCESS_INSERTED -> {
                    getProfileSuccessResponse.postValue(response.body())
                }
                Constant.UNPROCESSABLE_ENTITY -> {
                    unAuthorizationException.postValue(true)
                }
                else -> {
                    getProfileErrorResponse.postValue(response.errorBody())
                }
            }
        }
    }

    fun onDetach() {
        viewModelScope.cancel()
    }
}