package com.rap.sheet.model.SearchConatct

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

class SearchContactRootModel {
    @SerializedName("data")
    @Expose
    var data: MutableList<SearchContactDataModel>?=null

}