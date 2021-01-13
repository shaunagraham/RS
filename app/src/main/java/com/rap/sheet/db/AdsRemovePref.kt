package com.rap.sheet.db

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

class AdsRemovePref @SuppressLint("CommitPrefEdits") constructor(context: Context) {
    private val sharedPreferences: SharedPreferences
    private val editor: SharedPreferences.Editor
    private val PREFERENCE_NAME = "remove_ads_rapsheet"
    private val REMOVE_ADS = "remove_ads"

    fun setRemoveAdsData(isAdsRemove: Boolean?) {
        editor.putBoolean(REMOVE_ADS, isAdsRemove!!)
        editor.commit()
    }

    val isAdsRemove: Boolean
        get() = sharedPreferences.getBoolean(REMOVE_ADS, false)

    fun clearUserDataPref() {
        editor.clear()
        editor.commit()
    }

    init {
        sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
    }

}