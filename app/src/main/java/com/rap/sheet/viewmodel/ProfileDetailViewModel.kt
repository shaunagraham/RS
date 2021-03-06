package com.rap.sheet.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope

import com.rap.sheet.retrofit.EveryOneAPIInterface
import com.rap.sheet.utilitys.Constant.SUCCESS_INSERTED
import com.rap.sheet.utilitys.Constant.SUCCESS_STATUS
import com.rap.sheet.utilitys.Constant.UNPROCESSABLE_ENTITY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.ResponseBody

class ProfileDetailViewModel constructor(private val restInterface: EveryOneAPIInterface) : BaseViewModel() {

    val profileDetailSuccessResponse= MutableLiveData<ResponseBody>()
    val profileDetailErrorResponse= MutableLiveData<ResponseBody>()

    fun getProfileDetail(number:String,accountSid:String,accountToken:String){
        viewModelScope.launch(apiException() + Dispatchers.Main) {
            val response = restInterface.everyoneAPI(number,accountSid,accountToken)

            when (response.code()) {
                SUCCESS_STATUS,SUCCESS_INSERTED -> {
                    profileDetailSuccessResponse.postValue(response.body())
                }
                UNPROCESSABLE_ENTITY->{
                    unAuthorizationException.postValue(true)
                }
                else -> {
                    profileDetailErrorResponse.postValue(response.errorBody())
                }
            }

        }
    }

    fun onDetach() {
        viewModelScope.cancel()
    }

}
