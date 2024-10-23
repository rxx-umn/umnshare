package com.example.uts_lec

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import java.io.File

class MenuActivity : ComponentActivity(), GestureDetector.OnGestureListener {

    private lateinit var gestureDetector: GestureDetector
    private val REQUEST_CAMERA_PERMISSION = 100
    private val REQUEST_IMAGE_CAPTURE = 101
    private lateinit var photoFile: File

    // Tambahkan view untuk post image dan caption
    private lateinit var postImageView: ImageView
    private lateinit var postCaptionView: TextView

    // Firebase Database reference
    private lateinit var databaseRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Tautkan layout XML
        setContentView(R.layout.activity_menu)

        // Inisialisasi GestureDetector untuk mendeteksi swipe
        gestureDetector = GestureDetector(this, this)

        // Referensi ke postImageView dan postCaptionView untuk menampilkan gambar dan caption
        postImageView = findViewById(R.id.postImage)
        postCaptionView = findViewById(R.id.postCaption)

        // Firebase Database Reference
        databaseRef = FirebaseDatabase.getInstance().getReference("posts")

        // Tambahkan log untuk debugging
        Log.d("MenuActivity", "Checking for new posts...")

        // Ambil data dari Firebase dan tampilkan di MenuActivity
        loadPostsFromFirebase()

        // Referensi ke Gambar Profil
        val profileImage: ImageView = findViewById(R.id.profileImage)
        profileImage.setOnClickListener {
            Toast.makeText(this, "Profil diklik", Toast.LENGTH_SHORT).show()
        }

        // Referensi ke Tombol Everyone
        val everyoneButton: Button = findViewById(R.id.everyoneButton)
        everyoneButton.setOnClickListener {
            Toast.makeText(this, "Everyone button clicked!", Toast.LENGTH_SHORT).show()
        }

        // Referensi ke Ikon Pesan
        val messageIcon: ImageView = findViewById(R.id.messageIcon)
        messageIcon.setOnClickListener {
            Toast.makeText(this, "Message Icon Clicked!", Toast.LENGTH_SHORT).show()
        }

        // Referensi ke input pesan dan tombol kirim pesan
        val messageInput: EditText = findViewById(R.id.messageInput)
        val sendMessageIcon: ImageView = findViewById(R.id.sendMessageIcon)

        sendMessageIcon.setOnClickListener {
            val message = messageInput.text.toString().trim()
            if (message.isNotEmpty()) {
                Toast.makeText(this, "Pesan terkirim: $message", Toast.LENGTH_SHORT).show()
                messageInput.text.clear()
            } else {
                Toast.makeText(this, "Pesan tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            }
        }

        // Referensi ke Emoji Icon
        val emojiIcon: ImageView = findViewById(R.id.emojiIcon)
        emojiIcon.setOnClickListener {
            Toast.makeText(this, "Emoji Icon Clicked!", Toast.LENGTH_SHORT).show()
        }
    }

    // Fungsi untuk mengambil data dari Firebase Realtime Database
    private fun loadPostsFromFirebase() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Ambil semua data dari node "posts"
                for (postSnapshot in snapshot.children) {
                    val post = postSnapshot.getValue(Post::class.java)
                    if (post != null) {
                        // Gunakan Glide untuk menampilkan gambar
                        Glide.with(this@MenuActivity)
                            .load(post.imageUrl)
                            .into(postImageView)

                        // Set caption
                        postCaptionView.text = post.caption
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MenuActivity", "Database error: ${error.message}")
            }
        })
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        if (e1 == null) return false

        val diffX = e2.x - e1.x
        Log.d("MenuActivity", "Fling detected: diffX = $diffX")
        if (diffX > 50) {
            checkCameraPermission()
            return true
        }
        return false
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Izin kamera diberikan, buka kamera
            openCamera()
        } else {
            // Izin kamera ditolak
            Toast.makeText(this, "Izin kamera diperlukan untuk mengambil gambar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openCamera() {
        photoFile = File.createTempFile("photo", ".jpg", cacheDir)
        val photoURI = FileProvider.getUriForFile(
            this, "com.example.uts_lec.fileprovider", photoFile
        )
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        }
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val intent = Intent(this, AddCaptionActivity::class.java)
            intent.putExtra("capturedImagePath", photoFile.absolutePath)
            startActivity(intent)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDown(e: MotionEvent): Boolean = true
    override fun onShowPress(e: MotionEvent) {}
    override fun onSingleTapUp(e: MotionEvent): Boolean = true
    override fun onScroll(
        e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float
    ): Boolean = true
    override fun onLongPress(e: MotionEvent) {}

    // Buat class model Post untuk menyimpan data dari Firebase
    data class Post(
        val imageUrl: String = "",
        val caption: String = ""
    )
}
