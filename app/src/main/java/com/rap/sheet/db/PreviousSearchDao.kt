package com.rap.sheet.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.rap.sheet.model.SearchConatct.SearchContactDataModel

@Dao
interface PreviousSearchDao {
    @get:Query("SELECT * from previous_search order by last_added DESC")
    val previousSearchResult: LiveData<MutableList<SearchContactDataModel>>

    @Query("SELECT * from previous_search where id=:ids")
    fun getSearchRecord(ids: String?): SearchContactDataModel?

    // We do not need a conflict strategy, because the word is our primary key, and you cannot
    // add two items with the same primary key to the database. If the table has more than one
    // column, you can use @Insert(onConflict = OnConflictStrategy.REPLACE) to update a row.
    @Insert
    fun insert(searchDataModel: SearchContactDataModel?)

    @Query("DELETE FROM previous_search")
    fun deleteAll()

    @Query("SELECT * from previous_search where id=:id")
    fun isRecordExists(id: String?): Int

    @Query("UPDATE previous_search Set avg_rate =:rate Where id=:id")
    fun updateRecord(rate: String?, id: String?)

    @Delete
    fun deleteSingleRecord(searchContactDataModel: SearchContactDataModel?)

    @Query("DELETE FROM previous_search where id=:id")
    fun deleteSingleRecord(id: String?) //    @Update
    //    void updateContact(SearchContactDataModel searchDataModel);
}