package com.rap.sheet.model.SearchConatct

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.rap.sheet.model.ContactDetail.ContactDetailCommentModel
import java.io.Serializable

class CommentRootModel : Serializable {
    @SerializedName("data")
    @Expose
    var data: List<ContactDetailCommentModel>? = null

}