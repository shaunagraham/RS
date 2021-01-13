package com.rap.sheet.utilitys

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.rap.sheet.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher
import java.util.regex.Pattern

object Utility {

    private var previousFragment: Fragment? = null

    fun capitalize(capString: String?): String {
        val capBuffer: StringBuffer = StringBuffer()
        val capMatcher: Matcher = Pattern.compile("([a-z])([a-z]*)", Pattern.CASE_INSENSITIVE).matcher(capString)
        while (capMatcher.find()) {
            capMatcher.appendReplacement(capBuffer, capMatcher.group(1).toUpperCase() + capMatcher.group(2).toLowerCase())
        }
        return capMatcher.appendTail(capBuffer).toString()
    }

    fun applyToolBarTitle(context: Context?, toolbar: Toolbar?) {
        for (i in 0 until toolbar!!.childCount) {
            val view: View = toolbar.getChildAt(i)
            if (view is TextView) {
                val textViewToolBar: TextView = view
                if ((textViewToolBar.text == toolbar.title)) {
                    val typeface: Typeface? = ResourcesCompat.getFont((context)!!, R.font.avenir_medium)
                    textViewToolBar.typeface = typeface
                    break
                }
            }
        }
    }


    // For Get Today Date as String
    // @inputFormat ex: yyyy/MM/dd HH:mm:ss
    fun getTodayDate(inputFormat: String?): String {
        val simpleDateFormat: SimpleDateFormat = SimpleDateFormat(inputFormat, Locale.US)
        simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val dateToday: Date = Calendar.getInstance().time
        return simpleDateFormat.format(dateToday)
    }

    fun getLeftTime(stringDate: String?, givenInputFormat: String?, requireInputFormat: String?): String {
        var stringResult: String = ""
        try {
            val stringTodayDate: String = getTodayDate(givenInputFormat)
            val simpleDateFormat: SimpleDateFormat = SimpleDateFormat(givenInputFormat, Locale.US)
            simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val simpleDateFormatRequired: SimpleDateFormat = SimpleDateFormat(requireInputFormat, Locale.US)
            simpleDateFormatRequired.timeZone = TimeZone.getTimeZone("UTC")
            val toDayDate: Date = simpleDateFormat.parse(stringTodayDate)
            val secondDate: Date = simpleDateFormat.parse(stringDate)
            val diffInMillisec: Long = toDayDate.time - secondDate.time
            val elapsedDays: Long = TimeUnit.MILLISECONDS.toDays(diffInMillisec)
            val elapsedSeconds: Long = TimeUnit.MILLISECONDS.toSeconds(diffInMillisec)
            val elapsedMinutes: Long = TimeUnit.MILLISECONDS.toMinutes(diffInMillisec)
            val elapsedHours: Long = TimeUnit.MILLISECONDS.toHours(diffInMillisec)
            if (elapsedDays < 1) {
                if (elapsedHours > 0) {
                    stringResult = if ((elapsedHours == 1L)) elapsedHours.toString() + " hour ago" else "" + elapsedHours + " hours ago"
                } else if (elapsedMinutes > 0) {
                    stringResult = if ((elapsedMinutes == 1L)) elapsedMinutes.toString() + " minute ago" else "" + elapsedMinutes + " minutes ago"
                } else if (elapsedSeconds > 0) {
                    stringResult = elapsedSeconds.toString() + " secs ago"
                } else {
                    stringResult = "Just now"
                }
            } else if (elapsedDays == 1L) {
                stringResult = "Yesterday"
            } else {
                stringResult = simpleDateFormatRequired.format(secondDate)
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return stringResult
    }

    fun getRandomDoubleBetweenRange(min: Double, max: Double): Int {
        val random: Double = (Math.random() * ((max - min) + 1)) + min
        return random.toInt()
    }

    fun addFragmentForBackStack(fragmentManager: FragmentManager, name: String, loadedFragment: Fragment?,
                                data: String?) {
        var fragment: Fragment? = FragmentUtil.getFragmentByTagName(fragmentManager, name)

        // Because fragment two has been popup from the back stack, so it must be null.
        if (fragment == null) {
            fragment = loadedFragment
        }
        if (data != null) {
            val args: Bundle = Bundle()
            args.putString("param1", data)
            fragment!!.arguments = args
        }
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        if (fragmentManager.findFragmentByTag(name) == null) {
            fragmentTransaction.add(R.id.frameContainer, (fragment)!!, name)
        }
        if (previousFragment != null) fragmentTransaction.hide(previousFragment!!)
        fragmentTransaction.show((fragment)!!)
        fragmentTransaction.addToBackStack(name)
        previousFragment = fragment
        fragmentTransaction.commit()
        FragmentUtil.printActivityFragmentList(fragmentManager)
    }

    fun saveNewNumber(context: Context?, number: String?, name: String?) {
        val contactIntent: Intent = Intent(ContactsContract.Intents.Insert.ACTION)
        contactIntent.type = ContactsContract.RawContacts.CONTENT_TYPE
        contactIntent
                .putExtra(ContactsContract.Intents.Insert.NAME, name)
                .putExtra(ContactsContract.Intents.Insert.PHONE, number)
        (context as Activity?)!!.startActivityForResult(contactIntent, 1)
    }

    fun callUserCall(context: Context?, number: String?) {
        context!!.startActivity(Intent(Intent.ACTION_CALL).setData(Uri.parse("tel:" + number!!.replace("-", ""))))
    }

    fun callUserSMS(context: Context?, number: String?) {
        val sms_uri: Uri = Uri.parse("smsto:$number")
        val smsIntent: Intent = Intent(Intent.ACTION_SENDTO)
        // Set the data for the intent as the phone number.
        smsIntent.data = sms_uri
        // Add the message (sms) with the key ("sms_body").
        smsIntent.putExtra("sms_body", "")
        // If package resolves (target app installed), send intent.
        if (smsIntent.resolveActivity(context!!.packageManager) != null) {
            context.startActivity(smsIntent)
        } else {
            Log.e("Hello", "Can't resolve app for ACTION_SENDTO Intent.")
        }
    }

    fun validateEditText(text: String): Boolean {
        return !TextUtils.isEmpty(text) && text.trim { it <= ' ' }.length > 0
    }

    fun isValidEmail(email: String?): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidMobile(phone: String?): Boolean {
        return Patterns.PHONE.matcher(phone).matches()
    }


}