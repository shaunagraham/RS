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

class SplashViewModel constructor(private val restInterface: RestInterface) : BaseViewModel() {

    val createUserSuccessResponse= MutableLiveData<ResponseBody>()
    val createUserErrorResponse= MutableLiveData<ResponseBody>()
    val makeJsonForCreateUser=MutableLiveData<JsonObject>()

    fun createUser(jsonObject: JsonObject){
        viewModelScope.launch(apiException() + Dispatchers.Main) {
            val response = restInterface.createNewUser(jsonObject)

            when (response.code()) {
                SUCCESS_STATUS,SUCCESS_INSERTED -> {
                    createUserSuccessResponse.postValue(response.body())
                }
                UNPROCESSABLE_ENTITY->{
                    unAuthorizationException.postValue(true)
                }
                else -> {
                    createUserErrorResponse.postValue(response.errorBody())
                }
            }
        }
    }

    fun makeJsonForCreateUser(uuid:String){
        val jsonObject=JsonObject()
        jsonObject.addProperty("uuid",uuid)
        jsonObject.addProperty("email","")
        jsonObject.addProperty("password","")
        makeJsonForCreateUser.postValue(jsonObject)
    }


    fun onDetach() {
        viewModelScope.cancel()
    }

}
