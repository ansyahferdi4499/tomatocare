package com.example.ferdian

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.ferdian.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inisialisasi binding menggunakan ViewBinding
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.findViewById<android.widget.ImageView>(R.id.ivBack)
            .setOnClickListener { finish() }
    }
}
