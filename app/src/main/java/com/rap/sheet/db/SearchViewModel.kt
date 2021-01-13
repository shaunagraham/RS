package com.rap.sheet.db

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.rap.sheet.model.SearchConatct.SearchContactDataModel

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val previousSearchRepository: PreviousSearchRepository
    var allSearchData: LiveData<MutableList<SearchContactDataModel>>

    fun insert(searchDataModel: SearchContactDataModel?) {
        previousSearchRepository.insert(searchDataModel)
    }

    fun delete(searchDataModel: SearchContactDataModel?) {
        previousSearchRepository.delete(searchDataModel)
    }

    fun isExistsData(id: String?): Int {
        return previousSearchRepository.checkRecordExistsOrNot(id)
    }

    fun updateData(rate: String?, id: String?) {
        previousSearchRepository.updateContact(rate, id)
    }

    fun deleteSingleRecord(id: String?) {
        previousSearchRepository.deleteSingleRecord(id)
    }

    init {
        val searchDuo = SearchDatabase.getDatabase(application).previousSearchDao()
        previousSearchRepository = PreviousSearchRepository(searchDuo)
        allSearchData = previousSearchRepository.allSearchResult!!
    }

}