package com.rap.sheet.utilitys

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

object InternetConnection {
    fun checkConnection(context: Context?): Boolean {
        val connMgr: ConnectivityManager = context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo: NetworkInfo? = connMgr.activeNetworkInfo
        if (activeNetworkInfo != null) { // connected to the internet
            if (activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI) {
                // connected to wifi
                return true
            } else if (activeNetworkInfo.type == ConnectivityManager.TYPE_MOBILE) {
                // connected to the mobile provider's data plan
                return true
            }
        }
        return false
    }
}