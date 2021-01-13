package com.rap.sheet.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

open class BaseViewModel : ViewModel() {

    val noInternetException = MutableLiveData<String>()
    val timeOutException = MutableLiveData<Boolean>()
    val unAuthorizationException = MutableLiveData<Boolean>()

    fun apiException(type: String = ""): CoroutineExceptionHandler {

        return CoroutineExceptionHandler { _, throwable ->
            Log.d("Hello",throwable.message.toString())
            when (throwable) {
                is SocketTimeoutException -> timeOutException.postValue(true)
                is ConnectException, is HttpException, is UnknownHostException -> noInternetException.postValue(
                    type
                )
            }
        }
    }


}