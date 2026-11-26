package com.example.todoapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.todoapp.R

class ShareReceiverActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val text = intent?.getStringExtra(Intent.EXTRA_TEXT)?.trim().orEmpty()

        if (text.isBlank()) {
            Toast.makeText(this, R.string.empty_share_text, Toast.LENGTH_SHORT).show()
        } else {
            startActivity(EditorActivity.prefillIntent(this, text))
        }
        finish()
    }
}
