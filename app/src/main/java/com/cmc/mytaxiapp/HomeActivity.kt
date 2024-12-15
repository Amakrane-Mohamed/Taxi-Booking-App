package com.cmc.mytaxiapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton // Import ImageButton for the profile button
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    private lateinit var btnProfile: ImageButton // Change to ImageButton
    private lateinit var btnTakeRide: Button
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("DriverPreferences", MODE_PRIVATE)

        // Initialize buttons
        btnProfile = findViewById(R.id.btnProfile) // Correctly find ImageButton
        btnTakeRide = findViewById(R.id.btnTakeRide)

        // Check if the driver is registered before allowing navigation
        val isRegistered = sharedPreferences.getBoolean("is_registered", false)

        // Open Profile Activity only if the driver is registered
        btnProfile.setOnClickListener {
            if (isRegistered) {
                startActivity(Intent(this, ProfileActivity::class.java))
            } else {
                // Show a message or redirect to registration activity
                startActivity(Intent(this, DriverRegistrationActivity::class.java))
            }
        }

        // Open MainActivity for taking a ride
        btnTakeRide.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}