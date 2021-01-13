package com.rap.sheet.model.MyContact

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by mvayak on 01-08-2018.
 */
class ContactModel {
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
    var nickName: String? = null

    @SerializedName("email")
    @Expose
    var email: String? = null

    @SerializedName("avg_rate")
    @Expose
    var avgRate: String? = null

    @SerializedName("created_at")
    @Expose
    var createdAt: String? = null

    @SerializedName("updated_at")
    @Expose
    var updatedAt: String? = null

    @SerializedName("reverse_phone_api")
    @Expose
    var reverse_phone_api: String? = null

}