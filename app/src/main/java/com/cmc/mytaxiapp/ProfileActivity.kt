package com.cmc.mytaxiapp

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.WriterException
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvDriverName: TextView
    private lateinit var tvDriverEmail: TextView
    private lateinit var tvDriverPhone: TextView
    private lateinit var ivDriverImage: ImageView
    private lateinit var ivQRCode: ImageView

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize views
        tvDriverName = findViewById(R.id.tvDriverName)
        tvDriverEmail = findViewById(R.id.tvDriverEmail)
        tvDriverPhone = findViewById(R.id.tvDriverPhone)
        ivDriverImage = findViewById(R.id.ivDriverImage)
        ivQRCode = findViewById(R.id.ivQRCode)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("DriverPreferences", Context.MODE_PRIVATE)

        // Retrieve driver information from SharedPreferences
        val driverName = sharedPreferences.getString("driver_name", "No Name")
        val driverEmail = sharedPreferences.getString("driver_email", "No Email")
        val driverPhone = sharedPreferences.getString("driver_phone", "No Phone")
        val driverImageUri = sharedPreferences.getString("driver_image_uri", "")

        // Display the information in the UI
        tvDriverName.text = driverName
        tvDriverEmail.text = driverEmail
        tvDriverPhone.text = driverPhone

        // Load profile image using Glide if the URI is not empty
        if (!driverImageUri.isNullOrEmpty()) {
            Glide.with(this)
                .load(Uri.parse(driverImageUri))  // Load from URI
                .placeholder(R.drawable.default_profile_image) // Optional: placeholder
                .into(ivDriverImage)
        }

        // Generate and display the QR code
        val driverData = "Name: $driverName, Email: $driverEmail, Phone: $driverPhone"
        generateQRCode(driverData, ivQRCode)  // Pass the QR code data and the ImageView
    }

    // Method to generate a QR code
    private fun generateQRCode(data: String, imageView: ImageView) {
        val qrCodeWriter = QRCodeWriter()
        try {
            val bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 500, 500)

            // Convert the bit matrix to a bitmap
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }

            // Set the bitmap to the ImageView
            imageView.setImageBitmap(bitmap)

        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }
}