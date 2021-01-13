package com.rap.sheet.model.ContactDetail

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by mvayak on 18-07-2018.
 */
class ContactDetailDataModel {
    @SerializedName("id")
    @Expose
    var id: String? = null

    @SerializedName("user_id")
    @Expose
    var userId: String? = null

    @SerializedName("number")
    @Expose
    var number: String? = null

    @SerializedName("first_name")
    @Expose
    var firstName: String? = null

    @SerializedName("last_name")
    @Expose
    var lastName: String? = null

    @SerializedName("nick_name")
    @Expose
    var nickName: Any? = null

    @SerializedName("email")
    @Expose
    var email: String? = null

    @SerializedName("weblink")
    @Expose
    var weblink: String? = null

    @SerializedName("instagram")
    @Expose
    var instagram: String? = null

    @SerializedName("facebook")
    @Expose
    var facebook: String? = null

    @SerializedName("linkedin")
    @Expose
    var linkedin: String? = null

    @SerializedName("profile")
    @Expose
    var profile: String? = null

    @SerializedName("avg_rate")
    @Expose
    var avgRate: String? = null

    @SerializedName("reverse_phone_api")
    @Expose
    var reversePhoneApi: String? = null

    @SerializedName("created_at")
    @Expose
    var createdAt: String? = null

    @SerializedName("updated_at")
    @Expose
    var updatedAt: Any? = null

}