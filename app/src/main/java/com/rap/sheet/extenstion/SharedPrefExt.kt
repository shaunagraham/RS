package com.rap.sheet.extenstion

import android.content.SharedPreferences
import android.util.Log

internal var SharedPreferences.uID: String
    set(value) {
        edit().putString("rs_user_uid", value).apply()
    }
    get() {
        getString("rs_user_uid", "").let {
            return it.toString()
        }
    }

internal var SharedPreferences.email: String
    set(value) {
        edit().putString("rs_user_email", value).apply()
    }
    get() {
        getString("rs_user_email", "").let {
            return it.toString()
        }
    }

internal var SharedPreferences.isFirstTime: Boolean
    set(value) {
        edit().putBoolean("is_first", value).apply()
    }
    get() {
        getBoolean("is_first", true).let {
            return it
        }

    }

internal var SharedPreferences.userID: String
    set(value) {
        Log.i("TAG", "rs_user_id: " + value)
        edit().putString("rs_user_id", value).apply()
    }
    get() {
        Log.i("TAG", "rs_user_id:11 " + getString("rs_user_id", ""))
        getString("rs_user_id", "").let {
            return it.toString()
        }
    }

fun SharedPreferences.clear() {
    val editor = this.edit()
    editor.clear()
    editor.apply()
}