package com.rap.sheet.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.rap.sheet.model.SearchConatct.SearchContactDataModel

@Database(entities = [SearchContactDataModel::class], version = 1, exportSchema = false)
abstract class SearchDatabase : RoomDatabase() {
    abstract fun previousSearchDao(): PreviousSearchDao

    companion object {
        var INSTANCE: SearchDatabase? = null
        fun getDatabase(context: Context): SearchDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(SearchDatabase::class.java) {
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        SearchDatabase::class.java,
                        "rapsheet_search_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}