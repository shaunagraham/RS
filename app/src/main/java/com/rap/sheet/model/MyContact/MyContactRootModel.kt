package com.rap.sheet.model.MyContact

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by mvayak on 01-08-2018.
 */

class MyContactRootModel {
    @SerializedName("Contacts")
    @Expose
    var contacts: List<ContactModel>? = null
}