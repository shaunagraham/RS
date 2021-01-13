package com.rap.sheet.model.SearchConatct

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity(tableName = "previous_search")
class SearchContactDataModel {
    @PrimaryKey
    @ColumnInfo(name = "id")
    @SerializedName("id")
    @Expose
    var id: String =""

    @ColumnInfo(name = "user_id")
    @SerializedName("user_id")
    @Expose
    var userId: String? = null

    @ColumnInfo(name = "number")
    @SerializedName("number")
    @Expose
    var number: String? = null

    @ColumnInfo(name = "first_name")
    @SerializedName("first_name")
    @Expose
    var firstName: String? = null

    @ColumnInfo(name = "last_name")
    @SerializedName("last_name")
    @Expose
    var lastName: String? = null

    @ColumnInfo(name = "nick_name")
    @SerializedName("nick_name")
    @Expose
    var nickName: String? = null

    @ColumnInfo(name = "avg_rate")
    @SerializedName("avg_rate")
    @Expose
    var avgRate: String? = null

    @ColumnInfo(name = "created_at")
    @SerializedName("created_at")
    @Expose
    var createdAt: String? = null

    @ColumnInfo(name = "email")
    @SerializedName("email")
    @Expose
    var email: String? = null

    @ColumnInfo(name = "reverse_phone_api")
    @SerializedName("reverse_phone_api")
    @Expose
    var reverse_phone_api: String? = null

    @ColumnInfo(name = "updated_at")
    @SerializedName("updated_at")
    @Expose
    var updatedAt: String? = null

    @ColumnInfo(name = "last_added")
    @Expose
    @SerializedName("last_added")
    var lastAdded: String? = null

}