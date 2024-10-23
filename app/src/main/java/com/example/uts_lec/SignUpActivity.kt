package com.example.uts_lec

import android.app.DatePickerDialog
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
import java.util.*

class SignUpActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Tautkan layout XML
        setContentView(R.layout.activity_signup)

        // Inisialisasi FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Referensi ke input email, password, birthday
        val emailInput: EditText = findViewById(R.id.emailInput)
        val passwordInput: EditText = findViewById(R.id.passwordInput)
        val birthdayInput: EditText = findViewById(R.id.birthdayInput)

        // Tombol kembali (backButton)
        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            // Tutup activity saat tombol back diklik
            finish()
        }

        // Tombol Sign Up
        val signUpButton: Button = findViewById(R.id.signUpButton)
        signUpButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            // Validasi input email dan password
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Cek apakah email berakhiran @student.umn.ac.id
            if (!email.endsWith("@student.umn.ac.id")) {
                Toast.makeText(this, "Hanya mahasiswa UMN yang dapat mendaftar!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Daftarkan pengguna baru dengan Firebase Authentication
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Pendaftaran berhasil
                        Toast.makeText(this, "Sign Up berhasil", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, SignInActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // Pendaftaran gagal
                        Toast.makeText(this, "Sign Up gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // Tombol untuk memilih tanggal (DatePicker)
        birthdayInput.setOnClickListener {
            // Dapatkan tanggal saat ini
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            // Tampilkan DatePicker
            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                birthdayInput.setText(selectedDate)
            }, year, month, day)

            datePickerDialog.show()
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
