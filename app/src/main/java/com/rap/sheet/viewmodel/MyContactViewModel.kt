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

class MyContactViewModel constructor(private val restInterface: RestInterface) : BaseViewModel() {


    val myContactListSuccessResponse= MutableLiveData<ResponseBody>()
    val myContactListErrorResponse= MutableLiveData<ResponseBody>()
    val deleteContactSuccessResponse= MutableLiveData<ResponseBody>()
    val deleteContactErrorResponse= MutableLiveData<ResponseBody>()



    fun getContactList(id:String){
        viewModelScope.launch(apiException() + Dispatchers.Main) {
            val response = restInterface.getAllMyContact(id)

            when (response.code()) {
                SUCCESS_STATUS,SUCCESS_INSERTED -> {
                    myContactListSuccessResponse.postValue(response.body())
                }
                else -> {
                    myContactListErrorResponse.postValue(response.errorBody())
                }
            }

        }
    }

    fun deleteContact(contactId:String,userId:String){
        viewModelScope.launch(apiException() + Dispatchers.Main) {
            val response = restInterface.deleteMyContact(contactId,userId)

            when (response.code()) {
                SUCCESS_STATUS,SUCCESS_INSERTED -> {
                    deleteContactSuccessResponse.postValue(response.body())
                }
                else -> {
                    deleteContactErrorResponse.postValue(response.errorBody())
                }
            }

        }
    }

    fun onDetach() {
        viewModelScope.cancel()
    }

}
