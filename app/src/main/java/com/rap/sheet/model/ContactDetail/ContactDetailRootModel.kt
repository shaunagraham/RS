package com.rap.sheet.model.ContactDetail

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by mvayak on 18-07-2018.
 */

class ContactDetailRootModel : Serializable {
    @SerializedName("details")
    @Expose
    var details: ContactDetailDataModel? = null

    @SerializedName("comments")
    @Expose
    var comments: List<ContactDetailCommentModel>? = null

}