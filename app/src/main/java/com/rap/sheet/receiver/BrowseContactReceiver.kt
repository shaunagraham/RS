package com.rap.sheet.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BrowseContactReceiver constructor(private val mListener: RefreshContactList) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        mListener.onReceive(intent.getBooleanExtra("refresh_contact", false))
    }

    interface RefreshContactList {
        fun onReceive(isRefresh: Boolean?)
    }

}