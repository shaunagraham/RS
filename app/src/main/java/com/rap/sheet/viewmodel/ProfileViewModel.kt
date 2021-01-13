package com.rap.sheet.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.rap.sheet.retrofit.RestInterface
import com.rap.sheet.utilitys.Constant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import java.util.*

class ProfileViewModel constructor(private val restInterface: RestInterface) : BaseViewModel() {

    val uploadProfileSuccessResponse = MutableLiveData<ResponseBody>()
    val uploadProfileErrorResponse = MutableLiveData<ResponseBody>()

    fun uploadProfile(uuid: RequestBody, firstName: RequestBody, lastName: RequestBody, number: RequestBody, email: RequestBody, media: MultipartBody.Part) {
        viewModelScope.launch(apiException() + Dispatchers.Main) {
            Log.i("TAG", "uploadProfile: "+media)
            val response = if (media != null) {
                restInterface.updateProfile(uuid, firstName, lastName, email, number, media)
            } else {
                restInterface.updateProfileWithOutImage(uuid, firstName, lastName, email, number)
            }

            when (response.code()) {
                Constant.SUCCESS_STATUS, Constant.SUCCESS_INSERTED -> {
                    uploadProfileSuccessResponse.postValue(response.body())
                }
                else -> {
                    uploadProfileErrorResponse.postValue(response.errorBody())
                }
            }

        }
    }

    fun uploadProfileWithout(uuid: RequestBody, firstName: RequestBody, lastName: RequestBody, number: RequestBody, email: RequestBody) {
        viewModelScope.launch(apiException() + Dispatchers.Main) {
            val response =
                restInterface.updateProfileWithOutImage(uuid, firstName, lastName, email, number)

            when (response.code()) {
                Constant.SUCCESS_STATUS, Constant.SUCCESS_INSERTED -> {
                    uploadProfileSuccessResponse.postValue(response.body())
                }
                else -> {
                    uploadProfileErrorResponse.postValue(response.errorBody())
                }
            }

        }
    }


    fun onDetach() {
        viewModelScope.cancel()
    }


}