package me.abolfazl.nmock.view.auth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import me.abolfazl.nmock.R

class AuthActivity : AppCompatActivity() {

    companion object{
        // Error messages
        const val UNKNOWN_ERROR_MESSAGE = R.string.unknownException
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
    }
}