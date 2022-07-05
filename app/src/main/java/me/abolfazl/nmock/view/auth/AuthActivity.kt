package me.abolfazl.nmock.view.auth

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import me.abolfazl.nmock.R

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    companion object {
        // Error messages
        const val UNKNOWN_ERROR_MESSAGE = R.string.unknownException
    }

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
    }
}