package com.rap.sheet.view.settings

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.rap.sheet.R
import com.rap.sheet.databinding.ActivityRemoveresultBinding

class RemoveResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRemoveresultBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_removeresult)
        setView()
    }

    private fun setView() {
        setUpToolBar()
        binding.buttonCancel.setOnClickListener { onBackPressed() }
    }

    private fun setUpToolBar() {
        binding.toolbar.imgClose.setOnClickListener { onBackPressed() }
        binding.toolbar.tvTitle.text = resources.getString(R.string.remove_result)
    }
}