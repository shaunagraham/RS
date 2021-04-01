package com.rap.sheet.view.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rap.sheet.R
import kotlinx.android.synthetic.main.activity_removeresult.*
import kotlinx.android.synthetic.main.top_view_layout.view.*

class RemoveResultActivity : AppCompatActivity() {
    //    private lateinit var binding: ActivityRemoveresultBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_removeresult)
        setView()
    }

    private fun setView() {
        setUpToolBar()
        buttonCancel.setOnClickListener { onBackPressed() }
    }

    private fun setUpToolBar() {
        toolbar4.imgClose.setOnClickListener { onBackPressed() }
        toolbar4.tvTitle.text = resources.getString(R.string.remove_result)
    }
}