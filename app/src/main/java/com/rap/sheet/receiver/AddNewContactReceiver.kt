package com.rap.sheet.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AddNewContactReceiver constructor(private val mListener: OnNewContactListener) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        mListener.onNewContactReady(intent.getStringExtra("new_contact"))
    }

    interface OnNewContactListener {
        fun onNewContactReady(contact: String?)
    }

}