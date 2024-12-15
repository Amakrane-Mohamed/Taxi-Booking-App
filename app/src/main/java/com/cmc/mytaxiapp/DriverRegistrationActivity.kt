package com.cmc.mytaxiapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import android.provider.MediaStore
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.SharedPreferences

class DriverRegistrationActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etPassword: EditText
    private lateinit var ivProfileImage: ImageView
    private lateinit var btnPickImage: Button
    private lateinit var btnRegister: Button
    private lateinit var sharedPreferences: SharedPreferences

    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_registration)

        // Initialize views
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        etPassword = findViewById(R.id.etPassword)
        ivProfileImage = findViewById(R.id.ivProfileImage)
        btnPickImage = findViewById(R.id.btnPickImage)
        btnRegister = findViewById(R.id.btnRegister)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("DriverPreferences", Context.MODE_PRIVATE)

        // Check for permissions
        checkPermissions()

        // Set up image picker button
        btnPickImage.setOnClickListener {
            // Open the image picker intent
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // Handle registration button click
        btnRegister.setOnClickListener {
            val driverName = etName.text.toString()
            val driverEmail = etEmail.text.toString()
            val driverPhone = etPhone.text.toString()
            val driverPassword = etPassword.text.toString()

            // Save data to SharedPreferences
            val editor = sharedPreferences.edit()
            editor.putString("driver_name", driverName)
            editor.putString("driver_email", driverEmail)
            editor.putString("driver_phone", driverPhone)
            editor.putString("driver_password", driverPassword)
            editor.putString("driver_image_uri", sharedPreferences.getString("driver_image_uri", "")) // Save the image URI
            editor.putBoolean("is_registered", true) // Mark as registered
            editor.apply()

            // Move to the home activity
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish() // Close the registration activity
        }
    }

    // Handle the result of image picker intent
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE_REQUEST) {
            val selectedImageUri: Uri? = data?.data
            if (selectedImageUri != null) {
                ivProfileImage.setImageURI(selectedImageUri)

                // Save the URI to SharedPreferences
                val editor = sharedPreferences.edit()
                editor.putString("driver_image_uri", selectedImageUri.toString())
                editor.apply()
            }
        }
    }

    // Function to check for permissions
    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

            // Request permission if not granted
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                100)
        }
    }

    // Handle the result of permission request
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can pick images
            } else {
                Toast.makeText(this, "Permission required to pick an image", Toast.LENGTH_SHORT).show()
            }
        }
    }
}