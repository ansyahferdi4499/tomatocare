package com.example.ferdian

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.ferdian.R

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        // Menunda navigasi ke HomeFragment selama 3 detik
        Handler(Looper.getMainLooper()).postDelayed({
            goToHomeFragment()
        }, 2000L)
    }

    // Fungsi untuk berpindah ke HomeFragment
    private fun goToHomeFragment() {
        val intent = Intent(this, MainActivity::class.java) // Mengarahkan ke MainActivity, karena HomeFragment biasanya ada di dalam MainActivity
        startActivity(intent)
        finish() // Mengakhiri SplashScreenActivity agar tidak kembali lagi
    }
}
