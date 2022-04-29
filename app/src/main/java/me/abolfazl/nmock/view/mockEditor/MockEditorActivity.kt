package me.abolfazl.nmock.view.mockEditor

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import me.abolfazl.nmock.R
import me.abolfazl.nmock.databinding.ActivityMockEditorBinding

class MockEditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMockEditorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMockEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}