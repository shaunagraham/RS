package com.rap.sheet.db

import android.os.AsyncTask
import androidx.lifecycle.LiveData
import com.rap.sheet.model.SearchConatct.SearchContactDataModel

class PreviousSearchRepository(private val previousSearchDao: PreviousSearchDao) {

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    var allSearchResult: LiveData<MutableList<SearchContactDataModel>>? = null

    init {
        previousSearchDao.apply {
            allSearchResult = previousSearchResult
        }
    }

    fun checkRecordExistsOrNot(id: String?): Int {
        return previousSearchDao.isRecordExists(id)
    }

    // You must call this on a non-UI thread or your app will crash.
    // Like this, Room ensures that you're not doing any long running operations on the main
    // thread, blocking the UI.

    fun insert(searchDataModel: SearchContactDataModel?) {
        AsyncTask.execute {
            previousSearchDao.apply {
                this.insert(searchDataModel)
            }
        }
    }

    fun updateContact(rate: String?, id: String?) {
        previousSearchDao.updateRecord(rate, id)
    }

    fun delete(searchDataModel: SearchContactDataModel?) {
        AsyncTask.execute {
            previousSearchDao.apply {
                this.deleteSingleRecord(searchDataModel)
            }
        }
//        deleteAsyncTask(previousSearchDao).execute(searchDataModel)
    }

    fun deleteSingleRecord(id: String?) {
        AsyncTask.execute {
            previousSearchDao.apply {
                this.deleteSingleRecord(id)
            }
        }
//        deleteSingleTaskAsyncTask(previousSearchDao).execute(id)
    }


//
//    class insertAsyncTask internal constructor(private val mAsyncTaskDao: PreviousSearchDao?) : AsyncTask<SearchContactDataModel?, Void?, Void?>() {
//        protected override fun doInBackground(vararg params: SearchContactDataModel): Void? {
//
//            return null
//        }
//
//    }
//
//    private class deleteAsyncTask internal constructor(private val mAsyncTaskDao: PreviousSearchDao?) : AsyncTask<SearchContactDataModel?, Void?, Void?>() {
//        protected override fun doInBackground(vararg params: SearchContactDataModel): Void? {
//            mAsyncTaskDao!!.deleteSingleRecord(params[0])
//            return null
//        }
//
//    }
//
//    private class deleteSingleTaskAsyncTask internal constructor(private val mAsyncTaskDao: PreviousSearchDao?) : AsyncTask<String?, Void?, Void?>() {
//        protected override fun doInBackground(vararg params: String): Void? {
//            mAsyncTaskDao!!.deleteSingleRecord(params[0])
//            return null
//        }
//
//    }

    // Note that in order to unit test the WordRepository, you have to remove the Application
    // dependency. This adds complexity and much more code, and this sample is not about testing.
    // See the BasicSample in the android-architecture-components repository at
    // https://github.com/googlesamples
//    init {
//        val db: SearchDatabase? = SearchDatabase.getDatabase(application)
//        db?.apply {
//            previousSearchDao = this.previousSearchDao()
//            allSearchResult = previousSearchDao?.previousSearchResult
//        }
//
//    }
}