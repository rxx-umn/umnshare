package com.example.uts_lec

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*

class AddCaptionActivity : ComponentActivity() {

    private lateinit var capturedImageView: ImageView
    private lateinit var captionText: TextView
    private lateinit var cancelButton: ImageView
    private lateinit var sendButton: ImageView
    private lateinit var saveButton: ImageView
    private lateinit var progressBar: ProgressBar

    // Firebase Storage & Database
    private val storageRef = FirebaseStorage.getInstance().reference
    private val databaseRef = FirebaseDatabase.getInstance().getReference("posts")

    private var imageUri: Uri? = null
    private lateinit var imageBitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_caption)

        // Inisialisasi komponen
        capturedImageView = findViewById(R.id.capturedImageView)
        captionText = findViewById(R.id.captionText)
        cancelButton = findViewById(R.id.cancelButton)
        sendButton = findViewById(R.id.sendButton)
        saveButton = findViewById(R.id.saveButton)
        progressBar = findViewById(R.id.progressBar)

        // Ambil gambar path dari intent
        val imagePath = intent.getStringExtra("capturedImagePath") ?: ""
        if (imagePath.isNotEmpty()) {
            val imageFile = File(imagePath)
            imageBitmap = BitmapFactory.decodeFile(imageFile.absolutePath) // Decode file menjadi Bitmap
            capturedImageView.setImageBitmap(imageBitmap)
        }

        // Tombol Cancel
        cancelButton.setOnClickListener {
            finish() // Kembali ke halaman sebelumnya
        }

        // Tombol Save
        saveButton.setOnClickListener {
            saveImageToGallery(imageBitmap)
            Toast.makeText(this, "Gambar disimpan!", Toast.LENGTH_SHORT).show()
        }

        // Tombol Send
        sendButton.setOnClickListener {
            val caption = captionText.text.toString().trim()
            if (caption.isNotEmpty()) {
                Log.d("AddCaptionActivity", "Caption: $caption")
                uploadImageToFirebase(imageBitmap, caption)
            } else {
                Toast.makeText(this, "Tambahkan caption terlebih dahulu!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadImageToFirebase(image: Bitmap, caption: String) {
        // Tampilkan progress bar saat upload dimulai
        progressBar.visibility = View.VISIBLE

        // Konversi Bitmap ke byte array
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageData = baos.toByteArray()

        // Buat nama file gambar berdasarkan UUID
        val imageName = UUID.randomUUID().toString()
        val imageRef = storageRef.child("images/$imageName.jpg")

        Log.d("AddCaptionActivity", "Uploading image to Firebase Storage: $imageName")

        // Unggah gambar ke Firebase Storage
        val uploadTask = imageRef.putBytes(imageData)
        uploadTask.addOnSuccessListener {
            // Ambil URL dari gambar yang diunggah
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                Log.d("AddCaptionActivity", "Image URL retrieved successfully: $uri")
                saveDataToDatabase(uri.toString(), caption) // Simpan data ke database
            }.addOnFailureListener { exception ->
                progressBar.visibility = View.GONE // Sembunyikan progress bar jika gagal
                Log.e("AddCaptionActivity", "Failed to get download URL: ${exception.message}")
                Toast.makeText(this, "Gagal mengambil URL gambar: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            progressBar.visibility = View.GONE // Sembunyikan progress bar jika gagal
            Log.e("AddCaptionActivity", "Failed to upload image: ${exception.message}")
            Toast.makeText(this, "Gagal mengunggah gambar: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveDataToDatabase(imageUrl: String, caption: String) {
        // Simpan URL gambar dan caption ke Firebase Realtime Database
        val dataMap = hashMapOf(
            "imageUrl" to imageUrl,
            "caption" to caption
        )

        val newPostRef = databaseRef.push()
        newPostRef.setValue(dataMap).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                progressBar.visibility = View.GONE // Sembunyikan progress bar setelah sukses
                Log.d("AddCaptionActivity", "Data saved successfully: $imageUrl")
                Toast.makeText(this, "Gambar dan caption berhasil diunggah!", Toast.LENGTH_SHORT).show()

                // Arahkan ke MenuActivity setelah berhasil
                val intent = Intent(this, MenuActivity::class.java)
                intent.putExtra("imageUrl", imageUrl)
                intent.putExtra("caption", caption)
                startActivity(intent)
                finish()
            } else {
                progressBar.visibility = View.GONE // Sembunyikan progress bar jika gagal
                Log.e("AddCaptionActivity", "Failed to save data: ${task.exception?.message}")
                Toast.makeText(this, "Gagal menyimpan data ke database: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            progressBar.visibility = View.GONE
            Log.e("AddCaptionActivity", "Failed to save data: ${exception.message}")
            Toast.makeText(this, "Gagal menyimpan data: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveImageToGallery(image: Bitmap) {
        // Implementasi penyimpanan ke galeri
    }
}
