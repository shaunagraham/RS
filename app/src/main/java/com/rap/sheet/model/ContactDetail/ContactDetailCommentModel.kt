package com.rap.sheet.model.ContactDetail

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by mvayak on 18-07-2018.
 */
class ContactDetailCommentModel : Serializable {
    @SerializedName("id")
    @Expose
    var id: String? = null

    @SerializedName("user_id")
    @Expose
    var userId: String? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("rate")
    @Expose
    var rate: String? = null

    @SerializedName("contact_id")
    @Expose
    var contactId: String? = null

    @SerializedName("created_at")
    @Expose
    var createdAt: String? = null

    @SerializedName("updated_at")
    @Expose
    var updatedAt: Any? = null

}