package com.example.uts_lec

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Tautkan layout XML
        setContentView(R.layout.activity_signin)

        // Inisialisasi FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Referensi ke input email dan password
        val emailInput: EditText = findViewById(R.id.emailInput)
        val passwordInput: EditText = findViewById(R.id.passwordInput)

        // Tombol kembali (backButton)
        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            // Tutup Activity saat tombol back diklik
            finish()
        }

        // Tombol Sign In
        val signInButton: Button = findViewById(R.id.signInButton)
        signInButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            // Validasi input email dan password
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Cek apakah email berakhiran @student.umn.ac.id
            if (!email.endsWith("@student.umn.ac.id")) {
                Toast.makeText(this, "Hanya mahasiswa UMN yang dapat masuk!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Masuk dengan Firebase Authentication
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in berhasil
                        Toast.makeText(this, "Sign In berhasil", Toast.LENGTH_SHORT).show()

                        // Arahkan ke MenuActivity setelah berhasil Sign In
                        val intent = Intent(this, MenuActivity::class.java)
                        startActivity(intent)
                        finish() // Tutup SignInActivity agar pengguna tidak bisa kembali ke sini
                    } else {
                        // Jika Sign in gagal
                        Toast.makeText(this, "Sign In gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // Toggle visibility pada kolom password
        passwordInput.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (passwordInput.right - passwordInput.compoundDrawables[2].bounds.width())) {
                    if (isPasswordVisible) {
                        // Sembunyikan password
                        passwordInput.transformationMethod = PasswordTransformationMethod.getInstance()
                        passwordInput.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_visibility, 0)
                        isPasswordVisible = false
                    } else {
                        // Tampilkan password
                        passwordInput.transformationMethod = HideReturnsTransformationMethod.getInstance()
                        passwordInput.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_visibility_off, 0)
                        isPasswordVisible = true
                    }

                    // Pindahkan kursor ke akhir teks
                    passwordInput.setSelection(passwordInput.text.length)
                    return@setOnTouchListener true
                }
            }
            false
        }
    }
}
