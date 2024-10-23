package com.example.uts_lec

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.content.Intent
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ClickableSpan
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Menautkan XML layout dengan setContentView()
        setContentView(R.layout.activity_main)

        // Temukan TextView berdasarkan ID
        val textView1: TextView = findViewById(R.id.textView1)
        val textView2: TextView = findViewById(R.id.textView2)
        val textView3: TextView = findViewById(R.id.textView3)

        // Terapkan font custom ke TextView
        val customFont = ResourcesCompat.getFont(this, R.font.sora)
        if (customFont != null) {
            textView1.typeface = customFont
            textView2.typeface = customFont
            textView3.typeface = customFont
        }

        // Membuat teks "Already registered? Sign In"
        val text = "Already registered? Sign In"

        // Membuat SpannableString
        val spannableString = SpannableString(text)

        // Membuat ClickableSpan untuk teks "Sign In"
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Arahkan pengguna ke halaman Sign In
                val intent = Intent(this@MainActivity, SignInActivity::class.java)
                startActivity(intent)
            }
        }

        // Set span hanya untuk teks "Sign In"
        spannableString.setSpan(clickableSpan, 19, 27, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Set spannableString ke TextView
        textView3.text = spannableString

        // Menambahkan LinkMovementMethod agar teks bisa diklik
        textView3.movementMethod = LinkMovementMethod.getInstance()

        // Menambahkan aksi pada tombol Sign Up
        val signUpButton: Button = findViewById(R.id.btnSignUp)
        signUpButton.setOnClickListener {
            // Ke halaman SignUp
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
}
