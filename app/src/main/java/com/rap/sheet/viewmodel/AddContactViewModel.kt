package com.rap.sheet.viewmodel

import android.provider.MediaStore
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
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody

class AddContactViewModel constructor(private val restInterface: RestInterface) : BaseViewModel() {

    val addContactSuccessResponse = MutableLiveData<ResponseBody>()
    val addContactErrorResponse = MutableLiveData<ResponseBody>()

    fun addNewContact(number: RequestBody, firstName: RequestBody, lastName: RequestBody, email: RequestBody, weblink: RequestBody, insta: RequestBody, facebook: RequestBody, linked: RequestBody, userId: RequestBody, media: MultipartBody.Part) {
        viewModelScope.launch(apiException() + Dispatchers.Main) {
            val response = if (media != null) {
                restInterface.addNewContact(number, firstName, lastName, email, weblink, insta, facebook, linked, userId, media)
            } else {
                restInterface.addNewContactWithoutImage(number, firstName, lastName, email, weblink, insta, facebook, linked, userId)
            }

            when (response.code()) {
                SUCCESS_STATUS, SUCCESS_INSERTED -> {
                    addContactSuccessResponse.postValue(response.body())
                }
                else -> {
                    addContactErrorResponse.postValue(response.errorBody())
                }
            }

        }
    }

    fun onDetach() {
        viewModelScope.cancel()
    }
}
