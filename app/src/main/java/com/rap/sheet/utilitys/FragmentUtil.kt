package com.rap.sheet.utilitys

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

object FragmentUtil {
    val TAG_NAME_FRAGMENT: String = "ACTIVITY_FRAGMENT"

    // Get exist Fragment by it's tag name.
    fun getFragmentByTagName(fragmentManager: FragmentManager, fragmentTagName: String): Fragment? {
        var ret: Fragment? = null

        // Get all Fragment list.
        val fragmentList: List<Fragment>? = fragmentManager.fragments
        if (fragmentList != null) {
            val size: Int = fragmentList.size
            for (i in 0 until size) {
                val fragment: Fragment? = fragmentList.get(i)
                if (fragment != null) {
                    val fragmentTag: String? = fragment.tag
                    assert(fragmentTag != null)
                    if ((fragmentTag == fragmentTagName)) {
                        ret = fragment
                    }
                }
            }
        }
        return ret
    }

    // Print fragment manager managed fragment in debug log.
    fun printActivityFragmentList(fragmentManager: FragmentManager) {
        // Get all Fragment list.
        val fragmentList: List<Fragment>? = fragmentManager.fragments
        if (fragmentList != null) {
            val size: Int = fragmentList.size
            for (i in 0 until size) {
                val fragment: Fragment? = fragmentList.get(i)
                if (fragment != null) {
                    val fragmentTag: String? = fragment.tag
                    Log.d(TAG_NAME_FRAGMENT, fragmentTag.toString())
                }
            }
            Log.d(TAG_NAME_FRAGMENT, "***********************************")
        }
    }

    fun removeAllFragmentList(fragmentManager: FragmentManager?) {
        if (fragmentManager != null && fragmentManager.fragments != null) {
            val fragmentList: MutableList<Fragment> = fragmentManager.fragments
            fragmentList.clear()
        }
    }
}